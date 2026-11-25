import { useQuery, useQueryClient } from "@tanstack/react-query";
import { useNavigate, useParams } from "react-router-dom";
import { useEffect, useState, type FormEvent } from "react";
import { Button, Card, CardContent, CardDescription, CardHeader, CardTitle, Input, Label } from "../../components/common/ui";
import { dealsApi } from "../../api/dealsApi";
import { useAuth } from "../../auth/AuthContext";
import type { DealRequest } from "../../api/types";

export default function DealFormPage() {
  const { id } = useParams<{ id?: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { hasPermission } = useAuth();
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [form, setForm] = useState<DealRequest>(() => initialDeal());

  const dealQuery = useQuery({
    queryKey: ["deals", "detail", id],
    queryFn: () => dealsApi.get(id as string),
    enabled: !!id,
  });

  useEffect(() => {
    if (id && dealQuery.data) {
      // Hydrate the editable form once the fetched deal arrives.
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setForm(fromDealResponse(dealQuery.data));
    }
  }, [id, dealQuery.data]);

  const isEdit = !!id;

  if (!hasPermission("DEAL_WRITE")) {
    return <div className="space-y-4"><p className="text-sm text-slate-500">You do not have permission to create or edit deals.</p></div>;
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!form.dealTypeId) {
      setSubmitError("Deal type is required.");
      return;
    }
    try {
      if (isEdit && id) {
        await dealsApi.update(id, form);
      } else {
        await dealsApi.create(form);
      }
      await queryClient.invalidateQueries({ queryKey: ["deals"] });
      navigate(isEdit && id ? `/deals/${id}` : "/deals");
    } catch (error) {
      setSubmitError(error instanceof Error ? error.message : "Failed to save deal");
    }
  }

  function update<K extends keyof DealRequest>(key: K, value: DealRequest[K]) {
    setForm((current) => ({ ...current, [key]: value }));
  }

  return (
    <form className="space-y-4" onSubmit={handleSubmit}>
      <div>
        <h1 className="text-2xl font-semibold text-slate-900">{isEdit ? "Edit deal" : "New deal"}</h1>
        <p className="text-sm text-slate-500">Payload follows the existing DealRequest contract.</p>
      </div>

      {submitError ? <div className="rounded-md border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{submitError}</div> : null}

      <Card>
        <CardHeader>
          <CardTitle>Deal</CardTitle>
          <CardDescription>Deal master data and financial fields.</CardDescription>
        </CardHeader>
        <CardContent className="grid gap-4 sm:grid-cols-2">
          <TextInput label="Deal type *" value={form.dealTypeId} onChange={(v) => update("dealTypeId", v)} placeholder="Deal type id" />
          <TextInput label="Instrument code" value={form.instrumentCodeId ?? ""} onChange={(v) => update("instrumentCodeId", v || null)} placeholder="Instrument code id" />
          <TextInput label="User id" value={form.userId ?? ""} onChange={(v) => update("userId", v || null)} placeholder="User id" />
          <TextInput label="Submitted by" value={form.submittedById ?? ""} onChange={(v) => update("submittedById", v || null)} placeholder="Submitted by id" />
          <TextInput label="Approved by" value={form.approvedById ?? ""} onChange={(v) => update("approvedById", v || null)} placeholder="Approved by id" />
          <TextInput label="Rejected by" value={form.rejectedById ?? ""} onChange={(v) => update("rejectedById", v || null)} placeholder="Rejected by id" />
          <TextInput label="Verified by" value={form.verifiedById ?? ""} onChange={(v) => update("verifiedById", v || null)} placeholder="Verified by id" />
          <TextInput label="Currency" value={form.currencyId ?? ""} onChange={(v) => update("currencyId", v || null)} placeholder="Currency id" />
          <TextInput label="Counter currency" value={form.counterCurrencyId ?? ""} onChange={(v) => update("counterCurrencyId", v || null)} placeholder="Counter currency id" />
          <TextInput label="Our nostro" value={form.ourNostroId ?? ""} onChange={(v) => update("ourNostroId", v || null)} placeholder="Our nostro id" />
          <TextInput label="Their nostro" value={form.theirNostroId ?? ""} onChange={(v) => update("theirNostroId", v || null)} placeholder="Their nostro id" />
          <TextInput label="Counterparty" value={form.counterpartyId ?? ""} onChange={(v) => update("counterpartyId", v || null)} placeholder="Counterparty id" />
          <TextInput label="II counterparty" value={form.iiCounterpartyId ?? ""} onChange={(v) => update("iiCounterpartyId", v || null)} placeholder="II counterparty id" />
          <TextInput label="III counterparty" value={form.iiiCounterpartyId ?? ""} onChange={(v) => update("iiiCounterpartyId", v || null)} placeholder="III counterparty id" />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Financial amounts</CardTitle>
          <CardDescription>All values follow the backend numeric conventions (BigDecimal serialized as JSON numbers).</CardDescription>
        </CardHeader>
        <CardContent className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <NumberInput label="Allocated amount" value={form.allotedAmt} onChange={(v) => update("allotedAmt", v)} />
          <NumberInput label="Allocated units" value={form.allotedUnits} onChange={(v) => update("allotedUnits", v)} />
          <NumberInput label="Amount" value={form.amount} onChange={(v) => update("amount", v)} />
          <NumberInput label="Bid amount" value={form.bidAmt} onChange={(v) => update("bidAmt", v)} />
          <NumberInput label="Contra amount" value={form.contraAmt} onChange={(v) => update("contraAmt", v)} />
          <NumberInput label="Coupon" value={form.coupon} onChange={(v) => update("coupon", v)} />
          <NumberInput label="Discounted price per unit" value={form.discountedPricePerUnit} onChange={(v) => update("discountedPricePerUnit", v)} />
          <NumberInput label="Discounted value" value={form.discountedValue} onChange={(v) => update("discountedValue", v)} />
          <NumberInput label="Effective yield" value={form.effectiveYield} onChange={(v) => update("effectiveYield", v)} />
          <NumberInput label="Exchange rate" value={form.exchangeRate} onChange={(v) => update("exchangeRate", v)} />
          <NumberInput label="II rate" value={form.iiRate} onChange={(v) => update("iiRate", v)} />
          <NumberInput label="III rate" value={form.iiiRate} onChange={(v) => update("iiiRate", v)} />
          <NumberInput label="Interest rate" value={form.interestRate} onChange={(v) => update("interestRate", v)} />
          <NumberInput label="Payable amount" value={form.payableAmt} onChange={(v) => update("payableAmt", v)} />
          <NumberInput label="Rollover value amount" value={form.rolloverValueAmount} onChange={(v) => update("rolloverValueAmount", v)} />
          <NumberInput label="Total cost" value={form.totalCost} onChange={(v) => update("totalCost", v)} />
          <NumberInput label="Total payable" value={form.totalPayable} onChange={(v) => update("totalPayable", v)} />
          <TextInput label="Tenure (days)" value={form.tenureInDays === null ? "" : String(form.tenureInDays)} onChange={(v) => update("tenureInDays", v ? Number(v) : null)} placeholder="Tenure in days" />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Deal metadata</CardTitle>
          <CardDescription>Textual and status fields exactly as defined by DealRequest.</CardDescription>
        </CardHeader>
        <CardContent className="grid gap-4 sm:grid-cols-2">
          <TextInput label="BBG code" value={form.bbgCode ?? ""} onChange={(v) => update("bbgCode", v)} maxLength={40} />
          <TextInput label="Bond code" value={form.bondCode ?? ""} onChange={(v) => update("bondCode", v)} maxLength={40} />
          <TextInput label="CUSIP" value={form.cusipNo ?? ""} onChange={(v) => update("cusipNo", v)} maxLength={20} />
          <TextInput label="Contact mode" value={form.contactMode ?? ""} onChange={(v) => update("contactMode", v)} maxLength={20} />
          <TextInput label="Direction" value={form.direction ?? ""} onChange={(v) => update("direction", v)} maxLength={4} />
          <TextInput label="Direction 2" value={form.direction2 ?? ""} onChange={(v) => update("direction2", v)} maxLength={4} />
          <TextInput label="Document action" value={form.docAction ?? ""} onChange={(v) => update("docAction", v)} maxLength={2} />
          <TextInput label="Document status" value={form.docStatus ?? ""} onChange={(v) => update("docStatus", v)} maxLength={2} />
          <TextInput label="Help" value={form.help ?? ""} onChange={(v) => update("help", v)} maxLength={2000} />
          <TextInput label="Reject reason" value={form.rejectReason ?? ""} onChange={(v) => update("rejectReason", v)} maxLength={2000} />
          <TextInput label="Rollover deal ref" value={form.rolloverDealRefNo ?? ""} onChange={(v) => update("rolloverDealRefNo", v)} maxLength={40} />
          <TextInput label="Rollover option" value={form.rolloverOption ?? ""} onChange={(v) => update("rolloverOption", v)} maxLength={4} />
          <TextInput label="Rollover remarks" value={form.rolloverRemarks ?? ""} onChange={(v) => update("rolloverRemarks", v)} maxLength={2000} />
          <TextInput label="Series" value={form.series ?? ""} onChange={(v) => update("series", v)} maxLength={40} />
          <TextInput label="Trader" value={form.trader ?? ""} onChange={(v) => update("trader", v)} maxLength={120} />
          <TextInput label="Interest payment frequency" value={form.interestPaymentFrequency ?? ""} onChange={(v) => update("interestPaymentFrequency", v)} maxLength={40} />
          <TextInput label="Market type" value={form.marketType ?? ""} onChange={(v) => update("marketType", v)} maxLength={40} />
          <TextInput label="Memo ref no" value={form.memoRefNo ?? ""} onChange={(v) => update("memoRefNo", v)} maxLength={40} />
          <TextInput label="Our custodian" value={form.ourCustodian ?? ""} onChange={(v) => update("ourCustodian", v)} maxLength={120} />
          <TextInput label="Their custodian" value={form.theirCustodian ?? ""} onChange={(v) => update("theirCustodian", v)} maxLength={120} />
        </CardContent>
      </Card>

      <div className="flex justify-end gap-2">
        <Button type="button" variant="outline" onClick={() => navigate(-1)}>Cancel</Button>
        <Button type="submit">{isEdit ? "Save changes" : "Create deal"}</Button>
      </div>
    </form>
  );
}

function initialDeal(): DealRequest {
  return {
    dealTypeId: "",
    instrumentCodeId: null,
    userId: null,
    approvedById: null,
    rejectedById: null,
    submittedById: null,
    verifiedById: null,
    currencyId: null,
    counterCurrencyId: null,
    ourNostroId: null,
    theirNostroId: null,
    counterpartyId: null,
    iiCounterpartyId: null,
    iiiCounterpartyId: null,
    allotedAmt: null,
    allotedUnits: null,
    amount: null,
    bidAmt: null,
    contraAmt: null,
    coupon: null,
    discountedPricePerUnit: null,
    discountedValue: null,
    effectiveYield: null,
    exchangeRate: null,
    iiRate: null,
    iiiRate: null,
    interestRate: null,
    payableAmt: null,
    rolloverPreviousRate: null,
    rolloverValueAmount: null,
    totalCost: null,
    totalPayable: null,
    tenureInDays: null,
    bbgCode: null,
    bondCode: null,
    contactMode: null,
    cusipNo: null,
    description: null,
    direction: null,
    direction2: null,
    docAction: null,
    docStatus: null,
    help: null,
    iiContactMode: null,
    iiContactPersonOrCode: null,
    iiiContactMode: null,
    iiiContactPersonOrCode: null,
    interestPaymentFrequency: null,
    marketType: null,
    memoRefNo: null,
    ourCustodian: null,
    theirCustodian: null,
    rejectReason: null,
    rolloverDealRefNo: null,
    rolloverOption: null,
    rolloverRemarks: null,
    series: null,
    trader: null,
    biddingDate: null,
    dateApproved: null,
    dateRejected: null,
    dateSubmitted: null,
    dateVerified: null,
    dealDate: null,
    dealTime: null,
    lastInterestPaymentDate: null,
    maturityDate: null,
    memoApprovedDate: null,
    nextInterestPaymentDate: null,
    originalIssueDate: null,
    rolloverPreviousValueDate: null,
    valueDate: null,
    active: null,
    rollover: null,
    processed: null,
    version: null,
  };
}

function fromDealResponse(deal: import("../../api/types").DealResponse): DealRequest {
  return {
    dealTypeId: deal.dealTypeId,
    instrumentCodeId: deal.instrumentCodeId,
    userId: deal.userId,
    approvedById: deal.approvedById,
    rejectedById: deal.rejectedById,
    submittedById: deal.submittedById,
    verifiedById: deal.verifiedById,
    currencyId: deal.currencyId,
    counterCurrencyId: deal.counterCurrencyId,
    ourNostroId: deal.ourNostroId,
    theirNostroId: deal.theirNostroId,
    counterpartyId: deal.counterpartyId,
    iiCounterpartyId: deal.iiCounterpartyId,
    iiiCounterpartyId: deal.iiiCounterpartyId,
    allotedAmt: deal.allotedAmt,
    allotedUnits: deal.allotedUnits,
    amount: deal.amount,
    bidAmt: deal.bidAmt,
    contraAmt: deal.contraAmt,
    coupon: deal.coupon,
    discountedPricePerUnit: deal.discountedPricePerUnit,
    discountedValue: deal.discountedValue,
    effectiveYield: deal.effectiveYield,
    exchangeRate: deal.exchangeRate,
    iiRate: deal.iiRate,
    iiiRate: deal.iiiRate,
    interestRate: deal.interestRate,
    payableAmt: deal.payableAmt,
    rolloverPreviousRate: deal.rolloverPreviousRate,
    rolloverValueAmount: deal.rolloverValueAmount,
    totalCost: deal.totalCost,
    totalPayable: deal.totalPayable,
    tenureInDays: deal.tenureInDays,
    bbgCode: deal.bbgCode,
    bondCode: deal.bondCode,
    contactMode: deal.contactMode,
    cusipNo: deal.cusipNo,
    description: deal.description,
    direction: deal.direction,
    direction2: deal.direction2,
    docAction: deal.docAction,
    docStatus: deal.docStatus,
    help: deal.help,
    iiContactMode: deal.iiContactMode,
    iiContactPersonOrCode: deal.iiContactPersonOrCode,
    iiiContactMode: deal.iiiContactMode,
    iiiContactPersonOrCode: deal.iiiContactPersonOrCode,
    interestPaymentFrequency: deal.interestPaymentFrequency,
    marketType: deal.marketType,
    memoRefNo: deal.memoRefNo,
    ourCustodian: deal.ourCustodian,
    theirCustodian: deal.theirCustodian,
    rejectReason: deal.rejectReason,
    rolloverDealRefNo: deal.rolloverDealRefNo,
    rolloverOption: deal.rolloverOption,
    rolloverRemarks: deal.rolloverRemarks,
    series: deal.series,
    trader: deal.trader,
    biddingDate: deal.biddingDate,
    dateApproved: deal.dateApproved,
    dateRejected: deal.dateRejected,
    dateSubmitted: deal.dateSubmitted,
    dateVerified: deal.dateVerified,
    dealDate: deal.dealDate,
    dealTime: deal.dealTime,
    lastInterestPaymentDate: deal.lastInterestPaymentDate,
    maturityDate: deal.maturityDate,
    memoApprovedDate: deal.memoApprovedDate,
    nextInterestPaymentDate: deal.nextInterestPaymentDate,
    originalIssueDate: deal.originalIssueDate,
    rolloverPreviousValueDate: deal.rolloverPreviousValueDate,
    valueDate: deal.valueDate,
    active: deal.active,
    rollover: deal.rollover,
    processed: deal.processed,
    version: deal.version,
  };
}

function TextInput({
  label,
  value,
  onChange,
  placeholder,
  maxLength,
}: {
  label: string;
  value: string | null;
  onChange: (value: string) => void;
  placeholder?: string;
  maxLength?: number;
}) {
  return (
    <div className="space-y-1.5">
      <Label>{label}</Label>
      <Input value={value ?? ""} placeholder={placeholder} maxLength={maxLength} onChange={(event) => onChange(event.target.value)} />
    </div>
  );
}

function NumberInput({ label, value, onChange }: { label: string; value: number | null; onChange: (value: number | null) => void }) {
  return (
    <div className="space-y-1.5">
      <Label>{label}</Label>
      <Input
        type="number"
        value={value === null ? "" : String(value)}
        onChange={(event) => {
          const raw = event.target.value;
          onChange(raw === "" ? null : Number(raw));
        }}
      />
    </div>
  );
}

