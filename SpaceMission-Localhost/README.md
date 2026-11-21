# Space-Mission-Management-System
Java-based Space Mission Management System using OOP concepts

## Run it (one command)
Needs only a **Java JDK 17 or newer**. The database driver is already in `lib/`.

- **Windows:** double-click `run.bat`
- **macOS / Linux:** `./run.sh`

Then open **http://localhost:8080** (the script tries to open it for you). Press `Ctrl+C` to stop.
If port 8080 is busy on macOS/Linux, use `PORT=9090 ./run.sh`.

## What is inside
- `web/index.html` – frontend (register, launch, edit fuel, delete)
- `MissionWebApp.java` – backend web server and REST API (new `main` class)
- `MissionDatabase.java` – JDBC + SQL (INSERT, SELECT, UPDATE, DELETE)
- `Json.java` – small JSON helper
- Original OOP classes – kept as they were (`MissionRegistry` only gained `getMission` and `removeMission`)
- `schema.sql` – the table definition and sample SQL (the program creates the table by itself)

The original console program (`SpaceMissionManagement`) and Swing window (`MissionControlGUI`) are still in the folder.
Data is stored in the `spacemissiondb` folder; delete that folder to start with an empty database.
