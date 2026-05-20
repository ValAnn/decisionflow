const { defineConfig } = require("cypress");

module.exports = defineConfig({
  e2E: {
    baseUrl: "http://localhost:5173", // Укажи адрес, на котором у тебя запускается Vue
    setupNodeEvents(on, config) {
      // здесь настраиваются плагины, если понадобятся
    },
  },

  e2e: {
    baseUrl: "http://localhost:5173",
    setupNodeEvents(on, config) {
      // implement node event listeners here
    },
  },
});
