(function () {
  const fallbackCities = [
    { city: 'Yangon', summary: 'Urban centre', pm: null },
    { city: 'Mandalay', summary: 'Inland region', pm: null },
    { city: 'Naypyidaw', summary: 'Capital area', pm: null }
  ];

  function normalizePm(value) {
    if (value === null || value === undefined || value === '') return null;
    const numberValue = Number(value);
    return Number.isFinite(numberValue) ? numberValue : null;
  }

  function normalizeCityCard(rawCity) {
    return {
      city: rawCity?.city || 'Unknown',
      summary: rawCity?.summary || '',
      pm: normalizePm(rawCity?.pm ?? rawCity?.pm25 ?? rawCity?.pm2_5)
    };
  }

  function normalizeCityCards(payload) {
    if (!Array.isArray(payload) || payload.length === 0) {
      throw new Error('Invalid city data');
    }

    return payload.map(normalizeCityCard);
  }

  function demoCityCards() {
    return fallbackCities.map((city) => ({
      ...city,
      pm: Math.round(8 + Math.random() * 36)
    }));
  }

  function formatPm(value) {
    const pm = normalizePm(value);
    return pm === null ? '-' : pm.toFixed(pm % 1 === 0 ? 0 : 1);
  }

  function predictionLabel(pm) {
    if (pm === null) return '';
    if (pm <= 12) return 'Good';
    if (pm <= 35.4) return 'Moderate';
    if (pm <= 55.4) return 'Unhealthy for Sensitive Groups';
    return 'Unhealthy';
  }

  function forestClasses(pm) {
    if (pm === null) return '';
    if (pm <= 35.4) return 'filter-none';
    if (pm <= 55.4) return 'filter grayscale-0';
    return 'filter grayscale contrast-75';
  }

  function moodOverlayClass(pm) {
    if (pm === null) return 'opacity-0';
    if (pm <= 12) return 'opacity-0';
    if (pm <= 35.4) return 'opacity-10 bg-yellow-200/20';
    if (pm <= 55.4) return 'opacity-30 bg-orange-600/15';
    return 'opacity-40 bg-red-700/20';
  }

  window.layHtuRenderers = {
    demoCityCards,
    formatPm,
    forestClasses,
    moodOverlayClass,
    normalizeCityCards,
    normalizePm,
    predictionLabel
  };
})();
