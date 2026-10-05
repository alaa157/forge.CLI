"use client";
import { useEffect, useState } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import { AppShell } from "../../../components/app-shell";
import { api, PipelineDetail } from "../../../lib/api";

export default function PipelinePage() {
  const { id } = useParams<{ id: string }>();
  const [pipeline, setPipeline] = useState<PipelineDetail>();
  const [nodes, setNodes] = useState<string[]>([]);
  const [edges, setEdges] = useState<{ from: string; to: string }[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    api
      .pipeline(id)
      .then((p) => {
        setPipeline(p);
        try {
          const graph = JSON.parse(p.jobGraph) as {
            topologicalOrder?: string[];
            edges?: { from: string; to: string }[];
            adjacency?: Record<string, string[]>;
          };
          if (graph.topologicalOrder) setNodes(graph.topologicalOrder);
          if (graph.edges) setEdges(graph.edges);
          else if (graph.adjacency) {
            const e: { from: string; to: string }[] = [];
            for (const [from, tos] of Object.entries(graph.adjacency)) {
              for (const to of tos) e.push({ from, to });
            }
            setEdges(e);
            if (!graph.topologicalOrder) setNodes(Object.keys(graph.adjacency));
          }
        } catch {
          setNodes(["build", "test", "integration"]);
        }
      })
      .catch((e) => setError(e.message));
  }, [id]);

  return (
    <AppShell>
      <Link href="/pipelines" className="text-sm text-zinc-500">
        ← Pipelines
      </Link>
      <h1 className="mt-4 text-3xl font-semibold">{pipeline?.name ?? "Pipeline"}</h1>
      <p className="mt-2 text-zinc-500">
        {pipeline?.forgeciVersion ? `ForgeCI ${pipeline.forgeciVersion}` : "Definition, dependency graph, and history."}
      </p>
      {error && (
        <p className="mt-4 rounded-lg border border-red-900 bg-red-950/30 p-3 text-sm text-red-300">{error}</p>
      )}
      <section className="mt-8 rounded-xl border border-zinc-800 bg-zinc-900/50 p-6">
        <h2 className="text-sm font-medium text-zinc-400">Dependency graph</h2>
        <div className="mt-6 flex flex-wrap items-center gap-3 text-sm">
          {nodes.map((n, i) => (
            <span key={n} className="flex items-center gap-3">
              <span className="rounded-lg border border-cyan-800/50 bg-zinc-950 px-4 py-2 font-mono">{n}</span>
              {i < nodes.length - 1 && <span className="text-zinc-600">→</span>}
            </span>
          ))}
        </div>
        {edges.length > 0 && (
          <ul className="mt-4 space-y-1 font-mono text-xs text-zinc-500">
            {edges.map((e) => (
              <li key={`${e.from}-${e.to}`}>
                {e.from} → {e.to}
              </li>
            ))}
          </ul>
        )}
      </section>
    </AppShell>
  );
}
