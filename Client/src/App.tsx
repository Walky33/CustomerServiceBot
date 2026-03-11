import { useMemo, useState } from "react";
import Chat from "./components/Chat";
import Admin from "./components/Admin";
import Reports from "./components/Reports";

type Tab = "chat" | "admin" | "reports";

export default function App() {
  const [tab, setTab] = useState<Tab>("chat");

  const title = useMemo(() => {
    if (tab === "chat") return "Customer Service Bot";
    if (tab === "admin") return "Admin: Knowledge Base + Guidelines";
    return "Mistake Reports + Auto-fix";
  }, [tab]);

  return (
    <div style={styles.page}>
      <header style={styles.header}>
        <div style={styles.brand}>
          <div style={styles.logo}>🤖</div>
          <div>
            <div style={styles.h1}>{title}</div>
            <div style={styles.sub}>
              KB Q&A + intent routing + report + auto-fix overrides (Java backend)
            </div>
          </div>
        </div>

        <nav style={styles.nav}>
          <button
            style={tab === "chat" ? styles.tabActive : styles.tab}
            onClick={() => setTab("chat")}
          >
            Chat
          </button>
          <button
            style={tab === "admin" ? styles.tabActive : styles.tab}
            onClick={() => setTab("admin")}
          >
            Admin
          </button>
          <button
            style={tab === "reports" ? styles.tabActive : styles.tab}
            onClick={() => setTab("reports")}
          >
            Reports
          </button>
        </nav>
      </header>

      <main style={styles.main}>
        {tab === "chat" && <Chat />}
        {tab === "admin" && <Admin />}
        {tab === "reports" && <Reports />}
      </main>
    </div>
  );
}

const styles: Record<string, any> = {
  page: {
    fontFamily:
      'ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, "Helvetica Neue", Arial',
    padding: 16,
    background: "#0b1220",
    minHeight: "100vh",
    color: "#e8eefc"
  },
  header: {
    display: "flex",
    gap: 16,
    alignItems: "center",
    justifyContent: "space-between",
    padding: 16,
    borderRadius: 14,
    background: "rgba(255,255,255,0.06)",
    border: "1px solid rgba(255,255,255,0.12)"
  },
  brand: { display: "flex", gap: 12, alignItems: "center" },
  logo: {
    width: 44,
    height: 44,
    borderRadius: 12,
    display: "grid",
    placeItems: "center",
    background: "rgba(255,255,255,0.08)",
    border: "1px solid rgba(255,255,255,0.12)"
  },
  h1: { fontSize: 18, fontWeight: 800 },
  sub: { fontSize: 12, opacity: 0.8 },
  nav: { display: "flex", gap: 8 },
  tab: {
    padding: "10px 12px",
    borderRadius: 12,
    border: "1px solid rgba(255,255,255,0.12)",
    background: "rgba(255,255,255,0.04)",
    color: "#e8eefc",
    cursor: "pointer"
  },
  tabActive: {
    padding: "10px 12px",
    borderRadius: 12,
    border: "1px solid rgba(255,255,255,0.22)",
    background: "rgba(126, 231, 255, 0.12)",
    color: "#e8eefc",
    cursor: "pointer",
    fontWeight: 700
  },
  main: { marginTop: 16 }
};