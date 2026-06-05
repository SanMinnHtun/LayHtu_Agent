(function () {
  const api = window.layHtuApi;
  const renderers = window.layHtuRenderers;

  window.layHtuApp = function layHtuApp() {
    return {
      theme: localStorage.getItem('layhtu-theme') || (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'),
      openSettings: false,
      cards: [],
      tickerIndex: 0,
      prediction: null,
      lastUpdated: '--',
      statusMessage: 'Loading...',
      forestSrc: 'https://images.unsplash.com/photo-1501785888041-af3ef285b470?auto=format&fit=crop&w=1400&q=60',
      chatInput: '',
      openChat: false,
      loadingPrediction: false,

      get predictionDisplay() {
        if (this.prediction === null) return '-';
        return `${renderers.formatPm(this.prediction)} ug/m3`;
      },

      get forestClasses() {
        return renderers.forestClasses(this.prediction);
      },

      get moodOverlayClass() {
        return renderers.moodOverlayClass(this.prediction);
      },

      get predictionLabel() {
        return renderers.predictionLabel(this.prediction);
      },

      init() {
        this.applyTheme();

        setInterval(() => {
          if (this.cards.length > 0) this.tickerIndex = (this.tickerIndex + 1) % this.cards.length;
        }, 3500);

        this.fetchAllCities();
        this.fetchPrediction();

        setInterval(() => this.fetchAllCities(), 60_000);
        setInterval(() => this.fetchPrediction(), 60_000);
      },

      setTheme(theme) {
        this.theme = theme;
        localStorage.setItem('layhtu-theme', theme);
        this.applyTheme();
      },

      applyTheme() {
        if (this.theme === 'dark') {
          document.documentElement.classList.add('dark');
          document.documentElement.style.setProperty('--bg-from', '#0f172a');
          document.documentElement.style.setProperty('--bg-to', '#071133');
          document.documentElement.style.setProperty('--card-bg', 'rgba(10,10,10,0.36)');
          document.documentElement.style.setProperty('--card-border', 'rgba(255,255,255,0.03)');
          return;
        }

        document.documentElement.classList.remove('dark');
        document.documentElement.style.setProperty('--bg-from', '#ecfdf5');
        document.documentElement.style.setProperty('--bg-to', '#eef2ff');
        document.documentElement.style.setProperty('--card-bg', 'rgba(255,255,255,0.06)');
        document.documentElement.style.setProperty('--card-border', 'rgba(255,255,255,0.06)');
      },

      async fetchAllCities() {
        try {
          const data = await api.fetchCities();
          this.cards = renderers.normalizeCityCards(data);
          this.tickerIndex = Math.min(this.tickerIndex, Math.max(this.cards.length - 1, 0));
          this.statusMessage = 'City data updated';
        } catch (err) {
          console.warn('fetchAllCities failed, using demo data', err);
          this.cards = renderers.demoCityCards();
          this.tickerIndex = 0;
          this.statusMessage = 'City data (demo)';
        }
      },

      async fetchPrediction() {
        this.loadingPrediction = true;
        this.statusMessage = 'Calculating AI Prediction...';

        const controller = new AbortController();
        const timeout = setTimeout(() => controller.abort(), 19_000);

        try {
          const data = await api.fetchPrediction(controller.signal);
          const prediction = renderers.normalizePm(data?.prediction);
          if (prediction === null) throw new Error('Invalid prediction data');

          this.prediction = prediction;
          this.lastUpdated = new Date().toLocaleTimeString();
          this.statusMessage = 'Prediction updated';
        } catch (err) {
          console.warn('fetchPrediction error', err);
          this.onFetchError(err);
        } finally {
          clearTimeout(timeout);
          this.loadingPrediction = false;
        }
      },

      onFetchError(err) {
        if (err) console.warn('Fetch prediction error:', err);
        this.statusMessage = this.prediction === null
          ? 'Data gathering in progress...'
          : 'Prediction temporarily unavailable';
      },

      cityPmLabel(value) {
        return `${renderers.formatPm(value)} ug/m3`;
      },

      sendChat() {
        if (!this.chatInput.trim()) return;
        alert(`Chatbot demo: ${this.chatInput}`);
        this.chatInput = '';
      }
    };
  };
})();
