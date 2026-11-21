-- Reference SQL for the project. The program creates this table AUTOMATICALLY
-- on first start (MissionDatabase.initialize()), so you do not need to run it.

CREATE TABLE missions (
    mission_id    VARCHAR(50)  PRIMARY KEY,
    destination   VARCHAR(50)  NOT NULL,
    fuel          DOUBLE       NOT NULL,
    required_fuel DOUBLE       NOT NULL,
    status        VARCHAR(30)  NOT NULL,
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- INSERT: add a mission
INSERT INTO missions (mission_id, destination, fuel, required_fuel, status)
VALUES ('M101', 'Mars', 7000, 6000, 'READY');

-- SELECT: read missions
SELECT * FROM missions;
SELECT * FROM missions WHERE mission_id = 'M101';

-- UPDATE: change fuel and status
UPDATE missions SET fuel = 8000, status = 'READY' WHERE mission_id = 'M101';

-- DELETE: remove a mission
DELETE FROM missions WHERE mission_id = 'M101';
