package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"os"
	"time"
)

type registerRequest struct {
	WorkerID       string `json:"workerId,omitempty"`
	Hostname       string `json:"hostname"`
	Version        string `json:"version"`
	Capabilities   string `json:"capabilities"`
	MaxConcurrency int    `json:"maxConcurrency"`
}

type workerResponse struct {
	WorkerID string `json:"workerId"`
	Status   string `json:"status"`
}

func main() {
	controlPlane := env("FORGECI_CONTROL_PLANE", "http://localhost:8080")
	hostname, _ := os.Hostname()
	version := env("FORGECI_WORKER_VERSION", "0.1.0")

	workerID, err := register(controlPlane, hostname, version)
	if err != nil {
		log.Fatalf("register: %v", err)
	}
	log.Printf("registered worker %s", workerID)

	ticker := time.NewTicker(10 * time.Second)
	defer ticker.Stop()
	for range ticker.C {
		if err := heartbeat(controlPlane, workerID); err != nil {
			log.Printf("heartbeat failed: %v", err)
			continue
		}
		log.Printf("heartbeat ok worker=%s", workerID)
	}
}

func register(base, hostname, version string) (string, error) {
	body, _ := json.Marshal(registerRequest{
		Hostname: hostname, Version: version, Capabilities: `["docker"]`, MaxConcurrency: 1,
	})
	res, err := http.Post(base+"/internal/workers/register", "application/json", bytes.NewReader(body))
	if err != nil {
		return "", err
	}
	defer res.Body.Close()
	if res.StatusCode >= 300 {
		return "", fmt.Errorf("status %d", res.StatusCode)
	}
	var out workerResponse
	if err := json.NewDecoder(res.Body).Decode(&out); err != nil {
		return "", err
	}
	return out.WorkerID, nil
}

func heartbeat(base, workerID string) error {
	res, err := http.Post(base+"/internal/workers/"+workerID+"/heartbeat", "application/json", nil)
	if err != nil {
		return err
	}
	defer res.Body.Close()
	if res.StatusCode >= 300 {
		return fmt.Errorf("status %d", res.StatusCode)
	}
	return nil
}

func env(k, def string) string {
	if v := os.Getenv(k); v != "" {
		return v
	}
	return def
}
