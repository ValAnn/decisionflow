const { defineConfig } = require("cypress");
module.exports = defineConfig({
  e2E: {
    baseUrl: "http://localhost:5173",
    setupNodeEvents(on, config) {
    },
  },
  e2e: {
    baseUrl: "http://localhost:5173",
    setupNodeEvents(on, config) {
    },
  },
});
