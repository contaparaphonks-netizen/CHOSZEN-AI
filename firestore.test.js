const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write templates, projects, or tickets", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection(`users/${ALICE_UID}/templates`).get());
  await assertFails(unauthDb.collection(`users/${ALICE_UID}/projects`).get());
  await assertFails(unauthDb.collection(`users/${ALICE_UID}/tickets`).get());

  const now = new Date(Date.now() - 1000);
  await assertFails(
    unauthDb.collection(`users/${ALICE_UID}/tickets`).doc("t1").set({
      userId: ALICE_UID,
      subject: "Erro na exportacao",
      category: "Exportacao",
      priority: "alta",
      description: "Nao consigo exportar em 9:16",
      status: "aberto",
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Owner can create, list, partially update, and delete custom templates", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/templates`).doc("tpl_phonk_1").set({
      userId: ALICE_UID,
      name: "Neymar Phonk Aura",
      style: "Phonk Edit",
      category: "Futebol",
      durationSec: 18,
      suggestedMusic: "Brazilian Phonk 142 BPM",
      transitions: ["Flash Cut", "Whip Pan"],
      effects: ["Beat Shake", "Neon Glow", "Velocity Ramp"],
      aspectRatio: "9:16",
      contrast: 0.35,
      saturation: 0.25,
      brightness: -0.05,
      speedMultiplier: 1.25,
      watermarkText: "CHOSZEN™©",
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(aliceDb.collection(`users/${ALICE_UID}/templates`).get());

  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/templates`).doc("tpl_phonk_1").update({
      name: "Neymar Phonk v2",
      contrast: 0.45,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject ghost field & @DocumentId 'id' injection
  await assertFails(
    aliceDb.collection(`users/${ALICE_UID}/templates`).doc("tpl_bad").set({
      id: "tpl_bad",
      userId: ALICE_UID,
      name: "Invalido",
      style: "Phonk Edit",
      category: "Futebol",
      durationSec: 15,
      suggestedMusic: "Phonk",
      transitions: ["Cut"],
      effects: ["Glow"],
      aspectRatio: "9:16",
      contrast: 0.2,
      saturation: 0.2,
      brightness: 0.0,
      speedMultiplier: 1.0,
      watermarkText: "CHOSZEN™©",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Reject future timestamp
  await assertFails(
    aliceDb.collection(`users/${ALICE_UID}/templates`).doc("tpl_future").set({
      userId: ALICE_UID,
      name: "Futuro",
      style: "Phonk Edit",
      category: "Futebol",
      durationSec: 15,
      suggestedMusic: "Phonk",
      transitions: ["Cut"],
      effects: ["Glow"],
      aspectRatio: "9:16",
      contrast: 0.2,
      saturation: 0.2,
      brightness: 0.0,
      speedMultiplier: 1.0,
      watermarkText: "CHOSZEN™©",
      createdAt: new Date(Date.now() + 60000),
      updatedAt: new Date(Date.now() + 60000),
    })
  );

  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/templates`).doc("tpl_phonk_1").delete()
  );
});

test("Owner can create projects and support tickets with terminal state lock", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/projects`).doc("proj_1").set({
      userId: ALICE_UID,
      title: "Vini Jr UCL Highlights",
      subject: "Vini Jr.",
      style: "Velocity Edit",
      prompt: "Sincronizar dribles com batida phonk e zoom dinamico",
      aspectRatio: "9:16",
      qualityPreset: "Full HD",
      aiSummary: "Cortes em 0.0s, 2.4s, 5.1s com flash branco na batida.",
      watermarkText: "CHOSZEN™©",
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/tickets`).doc("ticket_1").set({
      userId: ALICE_UID,
      subject: "Duvida sobre exportacao 4K",
      category: "Qualidade",
      priority: "media",
      description: "Meu aparelho mostra limite ate Full HD, como funciona?",
      status: "aberto",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Close ticket to terminal state
  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/tickets`).doc("ticket_1").update({
      status: "resolvido",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Terminal state lock: cannot reopen or edit once resolvido
  await assertFails(
    aliceDb.collection(`users/${ALICE_UID}/tickets`).doc("ticket_1").update({
      status: "aberto",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Cross-user access is strictly denied across all subcollections", async () => {
  const created = new Date(Date.now() - 10000);
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await db.collection(`users/${BOB_UID}/templates`).doc("bob_tpl").set({
      userId: BOB_UID,
      name: "Bob Edit",
      style: "Aura Edit",
      category: "Futebol",
      durationSec: 20,
      suggestedMusic: "Aura Funk",
      transitions: ["Zoom"],
      effects: ["Aura"],
      aspectRatio: "9:16",
      contrast: 0.2,
      saturation: 0.1,
      brightness: 0.0,
      speedMultiplier: 1.0,
      watermarkText: "BOB",
      createdAt: created,
      updatedAt: created,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection(`users/${BOB_UID}/templates`).get());
  await assertFails(aliceDb.collection(`users/${BOB_UID}/templates`).doc("bob_tpl").get());
  await assertFails(
    aliceDb.collection(`users/${BOB_UID}/templates`).doc("bob_tpl").update({
      name: "Hacked",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(
    aliceDb.collection(`users/${BOB_UID}/templates`).doc("bob_tpl").delete()
  );
});
