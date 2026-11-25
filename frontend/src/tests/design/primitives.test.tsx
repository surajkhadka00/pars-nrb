import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { DataTable, PageHeader, StatusBadge } from "../../components/design/Primitives";

describe("design system primitives", () => {
  it("renders a page header with description", () => {
    render(<PageHeader title="Deals" description="Portfolio operations" />);
    expect(screen.getByRole("heading", { name: "Deals" })).toBeTruthy();
    expect(screen.getByText("Portfolio operations")).toBeTruthy();
  });

  it("renders status badges with accessible text", () => {
    render(<StatusBadge value="PENDING_REVIEW" />);
    expect(screen.getByText("PENDING_REVIEW")).toBeTruthy();
  });

  it("renders empty state for an empty table", () => {
    render(
      <DataTable<{ id: string }>
        columns={[{ key: "id", header: "ID", render: (row) => row.id }]}
        rows={[]}
        rowKey={(row) => row.id}
        emptyText="No deals found"
      />,
    );
    expect(screen.getByText("No deals found")).toBeTruthy();
  });
});
