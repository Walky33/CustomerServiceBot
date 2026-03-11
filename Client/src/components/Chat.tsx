import { useEffect, useRef, useState } from "react";
import { api } from "../api";
import type { Citation, ChatResponse } from "../api";

type Msg = { role: "user" | "bot"; text: string; citations?: Citation[] };

export default function Chat() {
  const [sessionId, setSessionId] = useState<string | null>(
    localStorage.getItem("sessionId")
  );

  const [input, setInput] = useState("");
  const [msgs, setMsgs] = useState<Msg[]>([
    {
      role: "bot",
      text:
        "Hi! Loading knowledge base…"
    }
  ]);

  const [lastQA, setLastQA] = useState<{
    question: string;
    botAnswer: string;
  } | null>(null);

  const [reportOpen, setReportOpen] = useState(false);
  const [reportText, setReportText] = useState("");
  const [expectedAnswer, setExpectedAnswer] = useState("");

  const [loading, setLoading] = useState(false);
  const [kbLabel, setKbLabel] = useState("Knowledge Base");
  const bottomRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [msgs, loading, reportOpen]);

  async function send() {
    const q = input.trim();
    if (!q) return;
    setInput("");
    setMsgs((m) => [...m, { role: "user", text: q }]);
    setLoading(true);

    try {
      const r: ChatResponse = await api.chat(sessionId, q);
      if (!sessionId) {
        setSessionId(r.sessionId);
        localStorage.setItem("sessionId", r.sessionId);
      }

      setMsgs((m) => [
        ...m,
        { role: "bot", text: r.answer, citations: r.citations }
      ]);
      setLastQA({ question: q, botAnswer: r.answer });
    } catch (e: any) {
      setMsgs((m) => [
        ...m,
        { role: "bot", text: `Error: ${e?.message || "failed to chat"}` }
      ]);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    (async () => {
      try {
        const cfg = await api.getConfig(); // we'll add this in api.ts if missing
        const label = kbNameFromUrl(cfg.kb_url);
        setKbLabel(label);

        // also update the initial welcome message
        setMsgs((prev) => {
          // If the first message is the loader message, replace it.
          if (prev.length === 1 && prev[0].role === "bot" && prev[0].text.includes("Loading knowledge base")) {
            return [
              {
                role: "bot",
                text: `Hi! Ask me anything about ${label}. If you’re checking **application status** or a **failed transaction**, I can help too.`
              }
            ];
          }

          // Otherwise, keep history and just add a small info note (optional)
          return prev;
        });
      } catch {
        // ignore; keep defaults
      }
    })();
  }, []);

  function kbNameFromUrl(url?: string) {
    if (!url) return "Knowledge Base";
    const u = url.toLowerCase();
    if (u.includes("atome-cash"))
        return "Atome Cash";
    if (u.includes("atome-card"))
        return "Atome Card";

    // fallback: try to use last part after category id
    const m = url.match(/categories\/\d+-([^/?#]+)/i);
    if (m?.[1]) {
      return m[1].replace(/-/g, " ").replace(/\b\w/g, (c) => c.toUpperCase());
    }
    return "Knowledge Base";
  }

  async function submitReport() {
    if (!lastQA) return;
    const fb = reportText.trim();
    if (!fb) return;

    await api.reportMistake({
      question: lastQA.question,
      botAnswer: lastQA.botAnswer,
      userFeedback: fb,
      expectedAnswer: expectedAnswer.trim() || undefined
    });

    setReportOpen(false);
    setReportText("");
    setExpectedAnswer("");
    setMsgs((m) => [
      ...m,
      {
        role: "bot",
        text:
          "Thanks — I’ve logged this as a mistake report. An admin can review it in the **Reports** tab and apply an auto-fix."
      }
    ]);
  }

  return (
    <div style={styles.wrap}>
      <div style={styles.chatBox}>
        {msgs.map((m, idx) => (
          <div
            key={idx}
            style={m.role === "user" ? styles.userRow : styles.botRow}
          >
            <div style={m.role === "user" ? styles.userMsg : styles.botMsg}>
              <div style={{ whiteSpace: "pre-wrap" }}>{m.text}</div>
              {m.citations && m.citations.length > 0 && (
                <div style={styles.cites}>
                  <div style={styles.citesTitle}>Sources</div>
                  {m.citations.map((c, i) => (
                    <a
                      key={i}
                      href={c.url}
                      target="_blank"
                      rel="noreferrer"
                      style={styles.citeLink}
                    >
                      {c.title}
                    </a>
                  ))}
                </div>
              )}
            </div>
          </div>
        ))}
        {loading && (
          <div style={styles.botRow}>
            <div style={styles.botMsg}>Thinking…</div>
          </div>
        )}
        <div ref={bottomRef} />
      </div>

      <div style={styles.composer}>
        <input
          style={styles.input}
          value={input}
          placeholder={`Try: "What are the requirements for ${kbLabel}?" or "My transaction failed"`}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => (e.key === "Enter" ? send() : null)}
        />
        <button style={styles.send} onClick={send} disabled={loading}>
          Send
        </button>

        <button
          style={styles.report}
          onClick={() => setReportOpen(true)}
          disabled={!lastQA}
          title={!lastQA ? "Send a message first" : "Report last bot answer"}
        >
          Report mistake
        </button>
      </div>

      {reportOpen && (
        <div style={styles.modalBackdrop} onClick={() => setReportOpen(false)}>
          <div style={styles.modal} onClick={(e) => e.stopPropagation()}>
            <div style={styles.modalTitle}>Report a mistake</div>

            <div style={styles.label}>Question</div>
            <div style={styles.readonlyBox}>{lastQA?.question}</div>

            <div style={styles.label}>Bot answer</div>
            <div style={styles.readonlyBox}>{lastQA?.botAnswer}</div>

            <div style={styles.label}>What’s wrong?</div>
            <textarea
              style={styles.textarea}
              value={reportText}
              onChange={(e) => setReportText(e.target.value)}
              placeholder="Example: It linked the wrong article / it misunderstood / the steps are incorrect…"
            />

            <div style={styles.label}>
              Optional: what should the correct answer be?
            </div>
            <textarea
              style={styles.textarea}
              value={expectedAnswer}
              onChange={(e) => setExpectedAnswer(e.target.value)}
              placeholder="Paste the correct answer (admin can use this to auto-fix)."
            />

            <div style={styles.modalActions}>
              <button style={styles.btnGhost} onClick={() => setReportOpen(false)}>
                Cancel
              </button>
              <button style={styles.btnPrimary} onClick={submitReport}>
                Submit report
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

const styles: Record<string, any> = {
  wrap: { display: "grid", gap: 12 },
  chatBox: {
    height: "65vh",
    overflow: "auto",
    padding: 14,
    borderRadius: 14,
    background: "rgba(255,255,255,0.06)",
    border: "1px solid rgba(255,255,255,0.12)"
  },
  userRow: { display: "flex", justifyContent: "flex-end", marginBottom: 10 },
  botRow: { display: "flex", justifyContent: "flex-start", marginBottom: 10 },
  userMsg: {
    maxWidth: 820,
    padding: 12,
    borderRadius: 14,
    background: "rgba(126, 231, 255, 0.16)",
    border: "1px solid rgba(126, 231, 255, 0.22)"
  },
  botMsg: {
    maxWidth: 820,
    padding: 12,
    borderRadius: 14,
    background: "rgba(255,255,255,0.05)",
    border: "1px solid rgba(255,255,255,0.12)"
  },
  cites: {
    marginTop: 10,
    paddingTop: 10,
    borderTop: "1px dashed rgba(255,255,255,0.18)",
    display: "grid",
    gap: 6
  },
  citesTitle: { fontSize: 12, opacity: 0.8 },
  citeLink: { color: "#bfe7ff", fontSize: 13, textDecoration: "underline" },

  composer: {
    display: "flex",
    gap: 10,
    alignItems: "center",
    padding: 12,
    borderRadius: 14,
    background: "rgba(255,255,255,0.06)",
    border: "1px solid rgba(255,255,255,0.12)"
  },
  input: {
    flex: 1,
    padding: "12px 12px",
    borderRadius: 12,
    border: "1px solid rgba(255,255,255,0.12)",
    background: "rgba(0,0,0,0.15)",
    color: "#e8eefc"
  },
  send: {
    padding: "12px 14px",
    borderRadius: 12,
    border: "1px solid rgba(255,255,255,0.12)",
    background: "rgba(255,255,255,0.08)",
    color: "#e8eefc",
    cursor: "pointer"
  },
  report: {
    padding: "12px 14px",
    borderRadius: 12,
    border: "1px solid rgba(255,115,115,0.35)",
    background: "rgba(255,115,115,0.12)",
    color: "#ffe9e9",
    cursor: "pointer"
  },

  modalBackdrop: {
    position: "fixed",
    inset: 0,
    background: "rgba(0,0,0,0.6)",
    display: "grid",
    placeItems: "center",
    padding: 14
  },
  modal: {
    width: "min(920px, 100%)",
    borderRadius: 14,
    background: "#0f1a2e",
    border: "1px solid rgba(255,255,255,0.12)",
    padding: 14,
    display: "grid",
    gap: 10
  },
  modalTitle: { fontSize: 16, fontWeight: 800 },
  label: { fontSize: 12, opacity: 0.85 },
  readonlyBox: {
    padding: 10,
    borderRadius: 12,
    background: "rgba(255,255,255,0.05)",
    border: "1px solid rgba(255,255,255,0.12)",
    whiteSpace: "pre-wrap",
    maxHeight: 160,
    overflow: "auto"
  },
  textarea: {
    width: "100%",
    minHeight: 80,
    padding: 10,
    borderRadius: 12,
    border: "1px solid rgba(255,255,255,0.12)",
    background: "rgba(0,0,0,0.15)",
    color: "#e8eefc",
    resize: "vertical"
  },
  modalActions: { display: "flex", gap: 10, justifyContent: "flex-end" },
  btnGhost: {
    padding: "10px 12px",
    borderRadius: 12,
    border: "1px solid rgba(255,255,255,0.12)",
    background: "transparent",
    color: "#e8eefc",
    cursor: "pointer"
  },
  btnPrimary: {
    padding: "10px 12px",
    borderRadius: 12,
    border: "1px solid rgba(126, 231, 255, 0.22)",
    background: "rgba(126, 231, 255, 0.16)",
    color: "#e8eefc",
    cursor: "pointer",
    fontWeight: 700
  }
};