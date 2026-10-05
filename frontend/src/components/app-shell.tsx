"use client";
import Link from "next/link";
export function AppShell({children}:{children:React.ReactNode}){
 return <div className="min-h-screen bg-[#09090b] text-zinc-100"><aside className="fixed inset-y-0 left-0 w-60 border-r border-zinc-800 bg-[#0c0c0f] p-5"><Link href="/" className="text-xl font-semibold tracking-tight">Forge<span className="text-cyan-400">CI</span></Link><nav className="mt-8 space-y-1 text-sm"><Nav href="/">Overview</Nav><Nav href="/repositories">Repositories</Nav><Nav href="/pipelines">Pipelines</Nav><Nav href="/tests">Tests</Nav><Nav href="/flaky-tests">Flaky tests</Nav><Nav href="/settings">Settings</Nav></nav></aside><main className="ml-60 min-h-screen p-8">{children}</main></div>
}
function Nav({href,children}:{href:string;children:React.ReactNode}){return <Link href={href} className="block rounded-lg px-3 py-2 text-zinc-400 hover:bg-zinc-800 hover:text-white">{children}</Link>}
