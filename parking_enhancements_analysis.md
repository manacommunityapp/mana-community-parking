# Parking Module Enhancement — Architectural Analysis

## Feature Scope

1. **EV Charging Station Management** — manage charger hardware, EV session tracking, and billing through the CFBOS wallet.
2. **IoT ANPR Gate Barrier Sync** — automatic number plate recognition cameras trigger barrier open/close events and update parking occupancy in real-time.

---

## Part 1 — EV Charging Station Management & CFBOS Billing Integration

### Overview

The EV charging system requires:
- Charger hardware device registration and status monitoring
- Resident EV session start/stop lifecycle
- Energy (kWh) metering per session
- Cost calculation and automatic debit from CFBOS resident wallet

### Where does this live?

> **✅ Stays in `mana-community-service` (this Java Spring Boot repo).** No separate repo needed.

The EV charging module is community-management domain logic — it's tightly coupled to:
- **`ParkingSlot`** (slot has charger capability)
- **`ResidentVehicle`** (vehicle is EV type)
- **CFBOS Wallet** (billing debit flow)

### New Classes Needed (Java Spring Boot)

```
parking/
├── entity/
│   ├── EvCharger.java              NEW — Charger device registration
│   ├── EvChargingSession.java      NEW — Session per vehicle per charger
│   └── EvChargingRate.java         NEW — Pricing tiers (peak/off-peak, flat/per-kWh)
├── engine/
│   └── EvBillingEngine.java        NEW — kWh × rate, peak/off-peak tiers, CFBOS wallet debit
├── dto/
│   ├── StartChargingRequest.java   NEW
│   ├── StopChargingRequest.java    NEW
│   └── ChargingSessionResponse.java NEW
├── repository/
│   ├── EvChargerRepository.java    NEW
│   └── EvChargingSessionRepository.java NEW
├── service/
│   └── EvChargingService.java      NEW — orchestrates session lifecycle + billing
└── controller/
    └── EvChargingController.java   NEW — REST endpoints
```

### Entity Design Sketch

```java
// EvCharger: the physical hardware device
public class EvCharger {
    Long id;
    ParkingSlot slot;          // which parking slot it is attached to
    String deviceId;           // hardware unique ID (OCPP/RFID device)
    String connectorType;      // Type2, CCS, CHAdeMO
    Double maxKwRating;        // e.g. 7.2 kW, 22 kW, 50 kW
    ChargerStatus status;      // AVAILABLE, CHARGING, FAULT, OFFLINE
    LocalDateTime lastHeartbeatAt;
}

// EvChargingSession: per resident usage
public class EvChargingSession {
    Long id;
    EvCharger charger;
    AppUser resident;
    ResidentVehicle vehicle;
    LocalDateTime startedAt;
    LocalDateTime endedAt;
    Double startMeterKwh;      // meter reading at start
    Double endMeterKwh;        // meter reading at end
    Double totalKwh;           // computed = end - start
    BigDecimal totalCost;      // debited from wallet
    SessionStatus status;      // ACTIVE, COMPLETED, INTERRUPTED
    Long cfbosWalletTransactionId; // FK to CFBOS wallet debit
}
```

### CFBOS Wallet Integration Flow

```
Resident taps "Start Charging"
    ↓
EvChargingService.startSession()
    → Validate slot has charger
    → Validate vehicle is EV
    → Check CFBOS wallet has sufficient balance (minimum hold)
    → Create EvChargingSession (ACTIVE)
    → Send OCPP StartTransaction command to charger hardware (via IoT Edge Service below)
    ↓
Session Running (real-time or polled)
    → Charger sends kWh meter telemetry (OCPP MeterValues event)
    → EvChargingService.updateSessionMeter()
    ↓
Resident taps "Stop Charging" (or auto-stop on full battery / time limit)
    ↓
EvChargingService.stopSession()
    → Compute totalKwh = endMeterKwh − startMeterKwh
    → EvBillingEngine.calculateCost(totalKwh, rate, peakHours)
    → WalletService.debit(residentId, totalCost, "EV Charging - Slot B12")
    → Mark session COMPLETED
    → Push push-notification + receipt
```

### `EvBillingEngine` Logic

| Rate Type | Logic |
|---|---|
| Flat per kWh | `totalCost = totalKwh × unitRate` |
| Peak/Off-peak tiers | Peak hours (7am–10am, 6pm–10pm) charged at 1.5× rate |
| Minimum billing | 30 min / 1 kWh minimum regardless of actual usage |
| Daily cap | Max ₹500 / session to protect against meter errors |

### REST Endpoints (Controller)

```
POST   /api/parking/ev/sessions/start       — Resident starts charging
POST   /api/parking/ev/sessions/{id}/stop   — Resident stops charging
GET    /api/parking/ev/sessions/{id}        — Session status (kWh so far, estimated cost)
GET    /api/parking/ev/sessions/my          — Resident charging history
GET    /api/parking/ev/chargers             — List chargers with availability
GET    /api/parking/ev/chargers/{id}/status — Real-time charger status
```

---

## Part 2 — IoT ANPR Gate Barrier Sync

### Overview

ANPR cameras at the entry/exit gate:
1. Capture a vehicle's number plate image
2. Run OCR (number plate recognition) on the edge device
3. Push the plate + gate event to the backend
4. The backend validates the plate against registered residents/visitors
5. If valid → open barrier + log entry/exit
6. If unknown → alert security guard in real-time

### Should this be a separate repo / language?

This is the key decision point:

---

### 🔵 Option A — Pure Java in `mana-community-service` (Recommended for simplicity)

The main service receives a webhook from the **ANPR camera's edge unit** (the camera's onboard firmware does the OCR — most modern ANPR cameras run Milestone, Genetec, or open ANPR firmware that publishes HTTP webhooks with the recognized plate).

```
ANPR Camera (edge OCR)
    ↓  HTTP POST webhook with { plate: "MH12AB1234", gateId: "GATE-MAIN-IN", confidence: 0.97 }
Spring Boot AnprController.java
    ↓
AnprGateService.java
    → Look up plate in ResidentVehicle table
    → If resident: log AnprGateEvent (ENTRY/EXIT), update ParkingSlot status, return { action: OPEN }
    → If visitor pass: validate pass validity, log event
    → If unknown: return { action: HOLD }, push WebSocket alert to security dashboard
    ↓
AnprGateEvent.java (audit trail: plate, gate, timestamp, resident/visitor/unknown)
```

**Pros:** Simple, no new infra, leverages existing resident/vehicle data, billing integration is native.
**Cons:** If the ANPR camera cannot do edge OCR and sends raw images, you need a computer vision service.

---

### 🟡 Option B — Python Microservice for OCR + Plate Recognition (When camera sends raw images)

If the camera sends raw image frames (RTSP/MJPEG stream or image snapshots), you need a computer vision layer. **This should be a separate Python repo** — Python has vastly superior ML/CV ecosystem (OpenCV, EasyOCR, PaddleOCR, YOLOv8 for plate detection).

**Recommended Python stack:**
- **Language:** Python 3.11+
- **Framework:** FastAPI (async, fast, OpenAPI auto-docs)
- **OCR Engine:** EasyOCR or PaddleOCR (Indian number plate formats)
- **Plate Detection:** YOLOv8 fine-tuned on Indian number plate dataset
- **GPU Acceleration:** NVIDIA CUDA or CPU fallback (sufficient for 2–5 FPS)

**Repo name suggestion:** `mana-anpr-service` (Python)

```
mana-anpr-service/ (Python)
├── main.py                    FastAPI entry point
├── detector/
│   ├── plate_detector.py      YOLOv8 bounding box detection
│   └── ocr_engine.py          EasyOCR text extraction
├── models/
│   └── yolov8_plate.pt        Fine-tuned weights (Indian plates)
├── api/
│   └── routes.py              POST /recognize  ← receives image frame
└── client/
    └── java_webhook_client.py → pushes result to mana-community-service webhook
```

**Communication flow:**
```
ANPR Camera (RTSP stream / image snapshot)
    ↓
mana-anpr-service (Python/FastAPI)
    → Detect plate bounding box (YOLOv8)
    → Extract text (EasyOCR) → "MH12AB1234"
    → POST { plate, gateId, confidence, timestamp }
        to mana-community-service /api/parking/anpr/webhook
    ↓
mana-community-service (Java Spring Boot)
    → Validates plate, opens barrier, logs event
```

---

### 🟠 Option C — Golang IoT Edge Gateway (Advanced, high throughput)

If you have 10+ gates and sub-100ms response requirement, consider a **Go microservice** as a lightweight IoT edge gateway.

- **Language:** Go 1.22+
- **Role:** MQTT broker subscriber, OCPP server for EV chargers, plate-match edge routing
- **Why Go:** Goroutines handle thousands of concurrent gate events cheaply

**Repo name suggestion:** `mana-iot-gateway` (Go)

This connects to:
- ANPR cameras via MQTT/WebSocket
- EV chargers via OCPP 1.6 / 2.0.1 protocol
- Pushes normalized events to `mana-community-service` via REST or Kafka

---

## Architecture Recommendation Matrix

| Scenario | Recommended Approach |
|---|---|
| ANPR camera has built-in OCR / firmware webhook | Java only — add `AnprGateService` to existing repo |
| ANPR camera sends raw images / RTSP stream | **Python microservice** (`mana-anpr-service`) + Java webhook receiver |
| 10+ gates, EV chargers, IoT scale | **Go IoT gateway** (`mana-iot-gateway`) as thin edge layer |
| EV billing integration | Java only — extend CFBOS `WalletService` + new `EvBillingEngine` |

---

## Complete System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    Hardware / IoT Layer                         │
│                                                                 │
│  [ANPR Camera]──────RTSP stream──────▶[mana-anpr-service]      │
│     OR                                     (Python/FastAPI)     │
│  [ANPR Camera w/ edge OCR]──webhook──▶     OCR + YOLOv8        │
│                                                │                │
│  [EV Charger OCPP]───OCPP 1.6──────▶[mana-iot-gateway]        │
│  [Barrier Controller]──────────────▶   (Go - optional)         │
└─────────────────────────────────────────────────────────────────┘
                              │ webhook / REST events
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              mana-community-service (Java Spring Boot)          │
│                                                                 │
│  AnprGateController ──▶ AnprGateService                        │
│       ↓ validate plate    ↓ match ResidentVehicle              │
│       ↓ open/hold barrier ↓ log AnprGateEvent                  │
│       ↓ push WebSocket to security dashboard                    │
│                                                                 │
│  EvChargingController ──▶ EvChargingService                    │
│       ↓ start/stop session ↓ EvBillingEngine                   │
│       ↓ CFBOS WalletService.debit(residentId, cost)            │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
                   ┌─────────────────┐
                   │  PostgreSQL DB  │
                   │  EvCharger      │
                   │  EvChargingSession│
                   │  AnprGateEvent  │
                   └─────────────────┘
```

---

## Additional Suggestions Beyond the Roadmap

### 🔌 EV Charging Enhancements
| Feature | Priority |
|---|---|
| **OCPP 2.0.1 compliance** — standard EV charger protocol support (required for certified charger hardware) | High |
| **Reservation / pre-booking** — residents book charger slot in advance via app | Medium |
| **Smart load balancing** — limit total community grid draw (e.g. max 40 kW across all chargers simultaneously) | High |
| **Carbon footprint dashboard** — show kWh saved vs petrol equivalent, CO₂ offset | Low |
| **Charger fault alerts** — FAULT status → immediate push notification to maintenance team | High |
| **Per-vehicle charging history** — full session history with cost breakdown in resident app | Medium |

### 📷 ANPR / Gate Enhancements
| Feature | Priority |
|---|---|
| **Multi-camera support** — main gate, back gate, basement entry, all with individual gate IDs | High |
| **Tailgating detection** — flag when 2 vehicles pass through on 1 authorization event | Medium |
| **Visitor pre-authorization** — resident pre-approves a guest plate → automatic entry | High |
| **Delivery vehicle whitelisting** — Swiggy/Zomato/Amazon vehicle class auto-permit during 9am–9pm | Medium |
| **Real-time security dashboard** — WebSocket-pushed gate event feed in the admin dashboard | High |
| **ANPR mis-read fallback** — when confidence < 0.85 → alert security guard on mobile app | High |
| **Stolen vehicle cross-check** — optional API integration with police VAHAN database | Low |
| **Gate analytics** — peak entry/exit hours, daily traffic volume, gate utilization heatmap | Medium |

### 🏗️ Infrastructure Considerations
| Item | Recommendation |
|---|---|
| **Message broker** | Add RabbitMQ or Kafka between IoT gateway and main service for reliable event delivery at scale |
| **OCPP server library** | Use `ocpp-j` (Java) or `python-ocpp` to avoid building OCPP protocol from scratch |
| **Edge deployment** | ANPR service can run on a Raspberry Pi 5 / Jetson Nano at the gate for low latency |
| **Database** | Add `TimescaleDB` extension to PostgreSQL for time-series charger telemetry (energy meter readings every 5s) |
| **Monitoring** | Prometheus metrics on charger OCPP heartbeats + gate event lag for ops alerting |

---

## Decision Summary

> **For your team size and current Spring Boot architecture, the recommended path is:**
>
> 1. ✅ **EV Charging (billing)** → implement entirely in `mana-community-service` (Java)
> 2. ✅ **ANPR webhook receiver** → implement in `mana-community-service` (Java)
> 3. 🆕 **ANPR OCR engine** → new `mana-anpr-service` Python repo **only if** your cameras do NOT have built-in OCR firmware. Most commercial ANPR cameras (Hikvision DS-2CD7A26G0, Dahua ITC series) already have edge OCR built in and just send HTTP webhooks.
> 4. 🆕 **IoT OCPP Gateway** → new `mana-iot-gateway` Go repo — recommended if you have 5+ EV chargers or need sub-50ms barrier response times.

Would you like to proceed with the implementation plan for any of these?
