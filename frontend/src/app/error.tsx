"use client";
export default function Error({reset}:{error:Error&{digest?:string};reset:()=>void}){return <main className="grid min-h-screen place-items-center"><button onClick={reset}>Try again</button></main>}
