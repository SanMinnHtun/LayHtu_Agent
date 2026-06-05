(function () {
  const jsonHeaders = { Accept: 'application/json' };

  async function fetchJson(url, options = {}) {
    const response = await fetch(url, {
      ...options,
      headers: { ...jsonHeaders, ...(options.headers || {}) }
    });

    const body = await response.json().catch(() => null);
    if (!response.ok) {
      const message = body?.message || body?.error || `${response.status} ${response.statusText}`;
      throw new Error(message);
    }

    return body;
  }

  window.layHtuApi = {
    fetchCities() {
      return fetchJson('/api/v1/weather/cities');
    },

    fetchPrediction(signal) {
      return fetchJson('/api/v1/weather/predict', { signal });
    }
  };
})();
