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

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
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

test("Unauthenticated user: cannot read wallpapers", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("wallpapers").get());
});

test("Authenticated user: cannot read another user's wallpapers", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).collection("wallpapers").doc("bob_wall_1").set({
      userId: BOB_UID,
      prompt: "rainy cyberpunk",
      aspectRatio: "9:16",
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("wallpapers").doc("bob_wall_1").get());
});

test("Authenticated user: can create and read their own wallpaper", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb.collection("users").doc(ALICE_UID).collection("wallpapers").doc("alice_wall_1");

  await assertSucceeds(
    docRef.set({
      userId: ALICE_UID,
      prompt: "rainy cyberpunk lo-fi neon reflections",
      aspectRatio: "9:16",
      imageSize: "1K",
      modelUsed: "gemini-nano-banana-2.1",
      isFavorite: true,
      variationNumber: 1,
      createdAt: new Date(),
    })
  );

  await assertSucceeds(docRef.get());
});

test("Authenticated user: cannot update immutable createdAt or userId", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("wallpapers").doc("alice_wall_1").set({
      userId: ALICE_UID,
      prompt: "rainy cyberpunk",
      aspectRatio: "9:16",
      createdAt: now,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb.collection("users").doc(ALICE_UID).collection("wallpapers").doc("alice_wall_1");

  // Attempting to change userId should fail
  await assertFails(
    docRef.update({
      userId: BOB_UID,
    })
  );
});
