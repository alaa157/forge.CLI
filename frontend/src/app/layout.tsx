import type {Metadata} from "next";
import {QueryProvider} from "../providers/query-provider";
import "./globals.css";
export const metadata:Metadata={title:"ForgeCI",description:"Self-hosted CI/CD and test-intelligence platform"};
export default function RootLayout({children}:{children:React.ReactNode}){return <html lang="en"><body><QueryProvider>{children}</QueryProvider></body></html>}
