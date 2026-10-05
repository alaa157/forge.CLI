const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const token =
    typeof window !== "undefined" ? localStorage.getItem("forgeci_access_token") : null;
  const res = await fetch(`${API_BASE}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(init?.headers ?? {}),
    },
    cache: "no-store",
  });
  if (!res.ok) throw new Error(`ForgeCI API ${res.status}: ${await res.text()}`);
  return res.status === 204 ? (undefined as T) : res.json();
}

export type Run = {
  id: string;
  repositoryId: string;
  commitSha: string;
  branch: string;
  trigger: string;
  status: string;
  createdAt: string;
  startedAt?: string;
  finishedAt?: string;
};

export type Job = {
  id: string;
  pipelineRunId: string;
  name: string;
  image: string;
  status: string;
  priority: string;
  attemptNumber?: number;
  lastFailureType?: string;
  maxRetries?: number;
  startedAt?: string;
  finishedAt?: string;
};

export type LogChunk = {
  id: string;
  sequence: number;
  stream: "stdout" | "stderr";
  content: string;
  createdAt: string;
};

export type PipelineDetail = {
  id: string;
  repositoryId: string;
  name: string;
  pipelineYaml: string;
  resolvedPipeline: string;
  jobGraph: string;
  forgeciVersion: string;
  lastRunAt?: string;
};

export const api = {
  runs: (repositoryId: string) =>
    request<Run[]>(`/api/v1/runs?repositoryId=${encodeURIComponent(repositoryId)}`),
  run: (id: string) => request<Run>(`/api/v1/runs/${id}`),
  jobs: (id: string) => request<Job[]>(`/api/v1/runs/${id}/jobs`),
  cancel: (id: string) => request<Run>(`/api/v1/runs/${id}/cancel`, { method: "POST" }),
  retry: (id: string) => request<Run>(`/api/v1/runs/${id}/retry`, { method: "POST" }),
  job: (id: string) => request<Job>(`/api/v1/jobs/${id}`),
  logs: (id: string) => request<LogChunk[]>(`/api/v1/jobs/${id}/logs?tail=500`),
  pipeline: (id: string) => request<PipelineDetail>(`/api/v1/pipelines/${id}`),
  pipelines: (repositoryId: string) =>
    request<PipelineDetail[]>(`/api/v1/pipelines?repositoryId=${encodeURIComponent(repositoryId)}`),
  flaky: (repositoryId: string) =>
    request<any[]>(`/api/v1/flaky-tests?repositoryId=${repositoryId}&limit=20`),
  dashboard: (repositoryId: string) =>
    request<Record<string, unknown>>(`/api/v1/repositories/${repositoryId}/tests/dashboard`),
};
