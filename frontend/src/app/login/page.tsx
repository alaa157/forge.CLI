"use client";
import { useState } from "react";
import { useRouter } from "next/navigation";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError("");
    try {
      const res = await fetch(`${API_BASE}/api/v1/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });
      if (!res.ok) throw new Error(await res.text());
      const tokens = await res.json();
      if (typeof window !== "undefined") {
        localStorage.setItem("forgeci_access_token", tokens.accessToken ?? tokens.access_token ?? "");
        localStorage.setItem("forgeci_refresh_token", tokens.refreshToken ?? tokens.refresh_token ?? "");
      }
      router.push("/");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Login failed");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-[#09090b] text-zinc-100">
      <form onSubmit={onSubmit} className="w-full max-w-sm rounded-2xl border border-zinc-800 bg-zinc-900/60 p-8">
        <h1 className="text-2xl font-semibold">Sign in to Forge<span className="text-cyan-400">CI</span></h1>
        <p className="mt-2 text-sm text-zinc-500">Use your control-plane credentials.</p>
        {error && <p className="mt-4 rounded-lg border border-red-900 bg-red-950/40 p-3 text-sm text-red-300">{error}</p>}
        <label className="mt-6 block text-sm text-zinc-400">Email
          <input className="mt-1 w-full rounded-lg border border-zinc-700 bg-zinc-950 px-3 py-2" type="email" value={email} onChange={e => setEmail(e.target.value)} required />
        </label>
        <label className="mt-4 block text-sm text-zinc-400">Password
          <input className="mt-1 w-full rounded-lg border border-zinc-700 bg-zinc-950 px-3 py-2" type="password" value={password} onChange={e => setPassword(e.target.value)} required />
        </label>
        <button disabled={loading} className="mt-6 w-full rounded-lg bg-cyan-500 py-2 font-medium text-zinc-950 hover:bg-cyan-400 disabled:opacity-50">
          {loading ? "Signing in…" : "Sign in"}
        </button>
      </form>
    </main>
  );
}
