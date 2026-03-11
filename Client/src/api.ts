export type Config = {
  kb_url: string;
  additional_guidelines: string;
};

export type Citation = { title: string; url: string };

export type ChatResponse = {
  answer: string;
  citations: Citation[];
  sessionId: string;
  canReportMistake: boolean;
};

export type Report = {
  id: string;
  question: string;
  botAnswer: string;
  userFeedback: string;
  expectedAnswer?: string | null;
  status: string;
  createdAt: string;
  resolvedAt?: string | null;
};

async function http<T>(url: string, options?: RequestInit): Promise<T> {
  const res = await fetch(url, {
    headers: { "Content-Type": "application/json" },
    ...options
  });
  if (!res.ok) {
    const text = await res.text().catch(() => "");
    throw new Error(text || `HTTP ${res.status}`);
  }
  return (await res.json()) as T;
}

async function getConfig() {
  const r = await fetch("/api/config");
  if (!r.ok) throw new Error("Failed to load config");
  return r.json();
}

export const api = {
  getConfig: () => http<Config>("/api/config"),
  setConfig: (cfg: Partial<Config>) =>
    http<Config>("/api/config", { method: "POST", body: JSON.stringify(cfg) }),

  rebuildKb: () => http<any>("/api/kb/rebuild", { method: "POST", body: "{}" }),

  chat: (sessionId: string | null, message: string) =>
    http<ChatResponse>("/api/chat", {
      method: "POST",
      body: JSON.stringify({ sessionId, message })
    }),

  reportMistake: (payload: {
    question: string;
    botAnswer: string;
    userFeedback: string;
    expectedAnswer?: string;
  }) =>
    http<Report>("/api/report", {
      method: "POST",
      body: JSON.stringify(payload)
    }),

  listOpenReports: () => http<Report[]>("/api/reports/open"),

  autoFix: (id: string, correctAnswer: string) =>
    http<{ ok: boolean; archivedReportId: string; demoNewAnswer: string }>(
      `/api/reports/${id}/autofix`,
      { method: "POST", body: JSON.stringify({ correctAnswer }) }
    )
};