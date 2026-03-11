import { useEffect, useState } from "react";
import { api } from "../api";
import type { Config } from "../api";

export default function Admin() {
  const [cfg, setCfg] = useState<Config | null>(null);
  const [kbUrl, setKbUrl] = useState("");
  const [guidelines, setGuidelines] = useState("");

  const [status, setStatus] = useState<string>("");

  useEffect(() => {
    (async () => {
      const c = await api.getConfig();
      setCfg(c);
      setKbUrl(c.kb_url);
      setGuidelines(c.additional_guidelines);
    })().catch((e) => setStatus(`Error: ${e.message}`));
  }, []);

  async function save() {
    setStatus("Saving…");
    try {
      const updated = await api.setConfig({
        kb_url: kbUrl,
        additional_guidelines: guidelines
      });
      setCfg(updated);
      setStatus("Saved ✅ (bot behavior is now updated)");
    } catch (e: any) {
      setStatus(`Save failed: ${e?.message || "unknown"}`);
    }
  }

  async function rebuild() {
    setStatus("Rebuilding KB index…");
    try {
      const r = await api.rebuildKb();
      setStatus(
        `KB rebuilt ✅ linksFound=${r.linksFound ?? r.result?.linksFound ?? "?"}, indexed=${r.indexed ?? r.result?.indexed ?? "?"}`
      );
    } catch (e: any) {
      setStatus(`Rebuild failed: ${e?.message || "unknown"}`);
    }
  }

  return (
    <div style={styles.wrap}>
      <div style={styles.card}>
        <div style={styles.h2}>Knowledge Base URL</div>
        <div style={styles.small}>
          This is the URL your bot crawls + indexes for answers.
        </div>
        <input
          style={styles.input}
          value={kbUrl}
          onChange={(e) => setKbUrl(e.target.value)}
        />

        <div style={{ height: 10 }} />

        <div style={styles.h2}>Additional Guidelines</div>
        <div style={styles.small}>
          Editable behavior notes (used for how the bot responds).
        </div>
        <textarea
          style={styles.textarea}
          value={guidelines}
          onChange={(e) => setGuidelines(e.target.value)}
        />

        <div style={styles.actions}>
          <button style={styles.btnPrimary} onClick={save}>
            Save changes
          </button>
          <button style={styles.btnGhost} onClick={rebuild}>
            Rebuild KB index
          </button>
        </div>

        {status && <div style={styles.status}>{status}</div>}
      </div>

      <div style={styles.card}>
        <div style={styles.h2}>Current Config (from server)</div>
        <pre style={styles.pre}>{JSON.stringify(cfg, null, 2)}</pre>
      </div>
    </div>
  );
}

const styles: Record<string, any> = {
  wrap: { display: "grid", gridTemplateColumns: "1.2fr 0.8fr", gap: 12 },
  card: {
    padding: 14,
    borderRadius: 14,
    background: "rgba(255,255,255,0.06)",
    border: "1px solid rgba(255,255,255,0.12)",
    display: "grid",
    gap: 10
  },
  h2: { fontSize: 14, fontWeight: 800 },
  small: { fontSize: 12, opacity: 0.8 },
  input: {
    width: "100%",
    padding: 12,
    borderRadius: 12,
    border: "1px solid rgba(255,255,255,0.12)",
    background: "rgba(0,0,0,0.15)",
    color: "#e8eefc"
  },
  textarea: {
    width: "100%",
    minHeight: 180,
    padding: 12,
    borderRadius: 12,
    border: "1px solid rgba(255,255,255,0.12)",
    background: "rgba(0,0,0,0.15)",
    color: "#e8eefc",
    resize: "vertical"
  },
  actions: { display: "flex", gap: 10, marginTop: 8 },
  btnPrimary: {
    padding: "10px 12px",
    borderRadius: 12,
    border: "1px solid rgba(126, 231, 255, 0.22)",
    background: "rgba(126, 231, 255, 0.16)",
    color: "#e8eefc",
    cursor: "pointer",
    fontWeight: 700
  },
  btnGhost: {
    padding: "10px 12px",
    borderRadius: 12,
    border: "1px solid rgba(255,255,255,0.12)",
    background: "transparent",
    color: "#e8eefc",
    cursor: "pointer"
  },
  status: {
    marginTop: 6,
    padding: 10,
    borderRadius: 12,
    background: "rgba(0,0,0,0.15)",
    border: "1px solid rgba(255,255,255,0.12)",
    fontSize: 13
  },
  pre: {
    margin: 0,
    padding: 12,
    borderRadius: 12,
    background: "rgba(0,0,0,0.15)",
    border: "1px solid rgba(255,255,255,0.12)",
    overflow: "auto",
    fontSize: 12
  }
};