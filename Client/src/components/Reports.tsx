import { useEffect, useState } from "react";
import { api } from "../api";
import type { Report } from "../api";

export default function Reports() {
  const [reports, setReports] = useState<Report[]>([]);
  const [status, setStatus] = useState<string>("");

  const [selected, setSelected] = useState<Report | null>(null);
  const [correctAnswer, setCorrectAnswer] = useState("");
  const [demo, setDemo] = useState<string>("");

  async function refresh() {
    setStatus("Loading reports…");
    try {
      const r = await api.listOpenReports();
      setReports(r);
      setStatus(`Loaded ${r.length} open report(s)`);
      if (r.length === 0) setSelected(null);
    } catch (e: any) {
      setStatus(`Error: ${e?.message || "failed to load reports"}`);
    }
  }

  useEffect(() => {
    refresh();
  }, []);

  function pick(r: Report) {
    setSelected(r);
    setCorrectAnswer(r.expectedAnswer || "");
    setDemo("");
  }

  async function autoFix() {
    if (!selected) return;
    const ans = correctAnswer.trim();
    if (!ans) {
      setStatus("Please enter the correct answer (needed for auto-fix).");
      return;
    }

    setStatus("Applying auto-fix…");
    try {
      const r = await api.autoFix(selected.id, ans);
      setDemo(r.demoNewAnswer);
      setStatus(`Auto-fix applied ✅ Report archived: ${r.archivedReportId}`);
      await refresh();
    } catch (e: any) {
      setStatus(`Auto-fix failed: ${e?.message || "unknown"}`);
    }
  }

  return (
    <div style={styles.wrap}>
      <div style={styles.left}>
        <div style={styles.card}>
          <div style={styles.h2}>Open Mistake Reports</div>
          <div style={styles.small}>
            Click a report to review. Use Auto-fix to create an override and archive it.
          </div>

          <div style={styles.list}>
            {reports.length === 0 && (
              <div style={styles.empty}>No open reports 🎉</div>
            )}
            {reports.map((r) => (
              <button
                key={r.id}
                style={
                  selected?.id === r.id ? styles.itemActive : styles.item
                }
                onClick={() => pick(r)}
              >
                <div style={styles.itemTitle}>
                  {r.question.slice(0, 70)}
                  {r.question.length > 70 ? "…" : ""}
                </div>
                <div style={styles.itemMeta}>
                  {new Date(r.createdAt).toLocaleString()} • {r.status}
                </div>
              </button>
            ))}
          </div>

          {status && <div style={styles.status}>{status}</div>}
        </div>
      </div>

      <div style={styles.right}>
        <div style={styles.card}>
          <div style={styles.h2}>Review + Fix</div>

          {!selected ? (
            <div style={styles.empty}>Select a report on the left.</div>
          ) : (
            <>
              <div style={styles.label}>Question</div>
              <div style={styles.readonly}>{selected.question}</div>

              <div style={styles.label}>Bot Answer</div>
              <div style={styles.readonly}>{selected.botAnswer}</div>

              <div style={styles.label}>User Feedback</div>
              <div style={styles.readonly}>{selected.userFeedback}</div>

              <div style={styles.label}>Correct Answer (will become override)</div>
              <textarea
                style={styles.textarea}
                value={correctAnswer}
                onChange={(e) => setCorrectAnswer(e.target.value)}
                placeholder="Write the corrected answer here. Auto-fix will save it and serve it for the same question in future."
              />

              <div style={styles.actions}>
                <button style={styles.btnPrimary} onClick={autoFix}>
                  Auto-fix + Archive
                </button>
                <button style={styles.btnGhost} onClick={refresh}>
                  Refresh
                </button>
              </div>

              {demo && (
                <>
                  <div style={styles.h3}>Demo: “Fixed” Answer (replayed)</div>
                  <div style={styles.readonly}>{demo}</div>
                  <div style={styles.small}>
                    This demo proves the override is now used for the same question.
                  </div>
                </>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
}

const styles: Record<string, any> = {
  wrap: { display: "grid", gridTemplateColumns: "0.9fr 1.1fr", gap: 12 },
  left: {},
  right: {},
  card: {
    padding: 14,
    borderRadius: 14,
    background: "rgba(255,255,255,0.06)",
    border: "1px solid rgba(255,255,255,0.12)",
    display: "grid",
    gap: 10
  },
  h2: { fontSize: 14, fontWeight: 800 },
  h3: { fontSize: 13, fontWeight: 800, marginTop: 6 },
  small: { fontSize: 12, opacity: 0.8 },
  list: { display: "grid", gap: 8, marginTop: 6 },
  item: {
    textAlign: "left",
    padding: 12,
    borderRadius: 12,
    border: "1px solid rgba(255,255,255,0.12)",
    background: "rgba(0,0,0,0.12)",
    color: "#e8eefc",
    cursor: "pointer"
  },
  itemActive: {
    textAlign: "left",
    padding: 12,
    borderRadius: 12,
    border: "1px solid rgba(126, 231, 255, 0.22)",
    background: "rgba(126, 231, 255, 0.12)",
    color: "#e8eefc",
    cursor: "pointer"
  },
  itemTitle: { fontWeight: 700, fontSize: 13 },
  itemMeta: { fontSize: 11, opacity: 0.75, marginTop: 4 },
  status: {
    marginTop: 8,
    padding: 10,
    borderRadius: 12,
    background: "rgba(0,0,0,0.15)",
    border: "1px solid rgba(255,255,255,0.12)",
    fontSize: 13
  },
  empty: {
    padding: 12,
    borderRadius: 12,
    background: "rgba(0,0,0,0.12)",
    border: "1px solid rgba(255,255,255,0.12)",
    fontSize: 13,
    opacity: 0.9
  },
  label: { fontSize: 12, opacity: 0.85 },
  readonly: {
    padding: 10,
    borderRadius: 12,
    background: "rgba(0,0,0,0.15)",
    border: "1px solid rgba(255,255,255,0.12)",
    whiteSpace: "pre-wrap",
    maxHeight: 180,
    overflow: "auto"
  },
  textarea: {
    width: "100%",
    minHeight: 140,
    padding: 12,
    borderRadius: 12,
    border: "1px solid rgba(255,255,255,0.12)",
    background: "rgba(0,0,0,0.15)",
    color: "#e8eefc",
    resize: "vertical"
  },
  actions: { display: "flex", gap: 10, marginTop: 6 },
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
  }
};