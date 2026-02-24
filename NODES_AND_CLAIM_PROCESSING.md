# Nodes and Their Role in the Claim Processing Application

This document explains what the **nodes** are, why they exist, and how they connect to the overall warranty claim processing application.

---

## 1. What Are the Nodes?

**Nodes** are **AST (Abstract Syntax Tree) nodes** extracted from RPG (RPGLE/SQLRPGLE) source programs. Each node represents a **code unit** inside an RPG program:

| Node kind        | Meaning in RPG |
|------------------|----------------|
| **CompilationUnit** | The whole program (top-level) |
| **Subroutine**   | A `BEGSR`/`ENDSR` block or similar reusable logic block |
| **Procedure**    | A `P` (procedure) definition — callable unit with its own scope |

- **Unit** = one RPG *program* (e.g. **HS1210**, **HS1212**).
- **Node** = one *part* of that program (e.g. **n404** = a specific subroutine in HS1210).

So **HS1210** is the program; **n404** is one subroutine inside it. The name **HS1210_n404** means: “node n404 in program HS1210”.

---

## 2. Why Do These Nodes Exist?

They exist because:

1. **RPG programs are large.** A single program (e.g. HS1210) can be thousands of lines and contain many subroutines and procedures. The AST breaks it into smaller, analyzable pieces.
2. **Migration is done per node.** Instead of migrating a whole program at once, we migrate **one node** (e.g. one subroutine) and get one focused “slice” of logic and its file/variable references.
3. **Each node has a clear scope:** its **semantic references** (which files and variables it uses) and **database contracts** (which physical files/tables and columns it touches). That scope is what gets turned into Java (entities, repositories, services).

So the nodes are the **granularity of migration**: one node → one context (e.g. `HS1210_n404.json`) → one Pure Java application (e.g. `HS1210_n404_pure_java`).

---

## 3. How HS1210 and Node n404 Relate to Claim Processing

### The program: HS1210

**HS1210** is an RPG program in the Scania warranty/claim domain. It lives in `HSSRC/QRPGLESRC/HS1210` (and possibly as HS1210.SQLRPGLE). It implements part of the **warranty claim processing** workflow (e.g. claim list, claim header/invoice, positions, errors, releases).

### The node: n404 (subroutine)

**n404** is a **Subroutine** inside HS1210. In the AST:

- **Range:** lines 824–2798 in HS1210 (a large block of logic).
- **Role:** It contains the core logic that:
  - Reads and updates **claims**, **invoices**, **work positions**, **errors**, **releases**, and **external service** data.
  - Uses the six database files listed below.

So **n404** is the “big” subroutine that ties together the main claim-processing data and operations in HS1210. Migrating **HS1210_n404** means migrating that entire slice of claim logic into a single Pure Java application.

### The six database files (and their Java counterparts)

Node n404’s context declares **six DB contracts** (physical files). These become the **entities** in the Pure Java app:

| RPG file (DB contract) | Java entity              | Role in claim processing |
|------------------------|--------------------------|---------------------------|
| **HSFLALF1**           | `HSFLALF1` (external service / FLA) | External service / FLA data (e.g. approvals, descriptions) |
| **HSAHKLF3**           | `Invoice`                | Invoice header/lines (rechnungen) — link between claim and invoice data |
| **HSAHWPF**             | `WorkPosition`           | Work positions (Arbeitspositionen) — labor/parts on a claim |
| **HSG70F**              | `ReleaseRequest` (SubmissionDeadlineRelease) | Submission/deadline release (e.g. for submission deadlines) |
| **HSG71LF2**           | `Claim`                  | **Claim header** — main claim record (claim nr, chassis, customer, dates, etc.) |
| **HSG73PF**            | `ClaimError`             | **Claim errors** — validation/processing errors per claim |

So in the **HS1210_n404_pure_java** application:

- **Claim** = main claim record (HSG71LF2).
- **Invoice** = invoice data (HSAHKLF3).
- **WorkPosition** = labor/parts (HSAHWPF).
- **ClaimError** = errors (HSG73PF).
- **ReleaseRequest** = release/deadline (HSG70F).
- **HSFLALF1** = external service/FLA (HSFLALF1).

Together they form the **data model** for the claim-processing slice that n404 implements.

---

## 4. How This Fits the “Overall” Claim Processing Application

Conceptually:

- **Overall claim processing** = everything the business does with warranty claims: create, search, validate, correct errors, manage invoices and positions, handle releases, etc.
- **HS1210** = one RPG program that implements a part of that (e.g. claim list/subfile, invoice and position handling, validations).
- **Node n404** = one large subroutine inside HS1210 that:
  - Operates on claims, invoices, positions, errors, releases, and external service data.
  - Is the “slice” we chose to migrate to get a **self-contained** Java application that can:
    - Search/list claims
    - Create claims
    - Work with invoices, positions, errors, and releases via the six entities above.

So **HS1210 n404** is not the entire company-wide “claim processing” system; it is **one major subroutine** that handles a central part of claim-related logic and data. The **HS1210_n404_pure_java** app is the Java counterpart of that subroutine — a single, deployable claim-processing “slice” with its own REST API (e.g. `/api/claims`, `/api/claims/search`).

---

## 5. Other Nodes in the Same Programs

From `context_index/manifest.json`:

### HS1210 (same program as n404)

| Node   | Kind             | DB files used | Likely role |
|--------|------------------|---------------|-------------|
| **n1**   | CompilationUnit  | FISTAM, S3F002 | Program-level / file definitions |
| **n404** | **Subroutine**   | **HSFLALF1, HSAHKLF3, HSAHWPF, HSG70F, HSG71LF2, HSG73PF** | **Main claim/invoice/position/error/release logic** (the one we migrated) |
| n1779 | Procedure        | HSG71PF, HSG73PF, HSGPSLF3 | Procedure using claim/error and GPS-related files |
| n1838 | Procedure        | —             | Procedure (no DB files in manifest) |
| n1919 | Procedure        | HSGPSPF       | Procedure (GPS-related) |
| n1983 | Procedure        | —             | Procedure (no DB files in manifest) |
| n2010 | Procedure        | —             | Procedure (no DB files in manifest) |
| n2020 | Procedure        | —             | Procedure (no DB files in manifest) |

So **n404** is the only node in HS1210 that uses the full set of six claim/invoice/position/error/release files we migrated. Other nodes are either the whole program (n1) or smaller procedures with different file usage.

### HS1212 (another program)

HS1212 has many nodes (n1, n498, n2575, n2614, …). These are other subroutines/procedures in a **different** RPG program (HS1212), which likely implements another part of the claim or warranty flow (e.g. another screen or batch job). They are **separate migration units**; migrating them would produce different context files (e.g. `HS1212_n498.json`) and potentially other Java apps.

---

## 6. Short Summary

- **Nodes** = AST nodes from RPG (CompilationUnit, Subroutine, Procedure). They are the **units of migration**.
- **HS1210** = one RPG program in the claim/warranty domain. **n404** = one large **subroutine** inside it.
- **HS1210 n404** uses six DB files (HSFLALF1, HSAHKLF3, HSAHWPF, HSG70F, HSG71LF2, HSG73PF) that map to **Claim**, **Invoice**, **WorkPosition**, **ClaimError**, **ReleaseRequest**, and **HSFLALF1** in Java.
- **HS1210_n404_pure_java** is the **claim-processing slice** that corresponds to that one subroutine: it is the part of the “overall” claim application that this node implements, exposed as a single Java application with REST APIs for claims, search, and related data.

If you want, the next step can be to map **HS1212** nodes to the same picture (e.g. which procedures do what and how they’d connect to the same or different Java services).
