import { describe, expect, it, vi } from "vitest";
import { aiApi } from "../../api/aiApi";

const mocks = vi.hoisted(() => ({ post: vi.fn() }));

vi.mock("../../api/client", () => ({
  api: { post: mocks.post, get: vi.fn(), delete: vi.fn() },
  unwrap: (response: { data: { data: unknown } }) => response.data.data,
}));

describe("aiApi", () => {
  it("posts a chat question to the AI endpoint", async () => {
    mocks.post.mockResolvedValue({ data: { data: { answer: "test" } } });
    const result = await aiApi.chat("What is the deal approval process?");
    expect(mocks.post).toHaveBeenCalledWith("/api/v1/ai/chat", {
      question: "What is the deal approval process?",
    });
    expect(result).toEqual({ answer: "test" });
  });
});
