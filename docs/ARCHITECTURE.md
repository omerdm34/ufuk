# Mimari

Tek `:app` modülü, **feature-first** paketleme; her özellik kendi `data / domain / presentation` katmanına sahiptir (taslak uygulamayla aynı yaklaşım). Bağımlılık yönü `presentation → domain ← data`. Katman ve özellik sınırlarını `LayerDependencyTest` denetler.

## Paket haritası

```
com.kampplus.ufuk
├── UfukApplication / MainActivity (splash, edge-to-edge) / UfukApp (Scaffold + NavigationBar)
├── core/
│   ├── common/      AppResult (+ runCatchingApp), AppError, ErrorMapper, dispatcher qualifier'ları,
│   │                Ticker (her 30 sn "şimdi"), CommonModule
│   ├── model/       Paylaşılan çekirdek: Place, Coordinates (+ cacheKey), UnitSettings, TemperatureUnit, WindSpeedUnit
│   ├── network/     NetworkModule (@ForecastRetrofit / @GeocodingRetrofit / @AirQualityRetrofit), NetworkErrorMapper
│   ├── database/    UfukDatabase (v2, AutoMigration 1→2), DatabaseModule
│   ├── datastore/   DataStoreModule (tek Preferences DataStore)
│   ├── navigation/  Destinations (type-safe), TopLevelDestination, UfukNavigationBar, UfukNavHost (composition root)
│   └── ui/          theme (lake/gümüş renk rolleri, InstrumentColors, Barlow tipografisi), motion (azaltılmış hareket),
│                    component (kadran çizimi, WeatherGlyph, durum ekranları, FreshnessNote, SaveToggleButton, degreesText),
│                    format (UnitFormatter), state (UiState), text (UiText + çoğul, AppErrorText)
└── feature/
    ├── forecast/
    │   ├── domain/  model (Forecast, CurrentConditions, Hourly/DailyForecast, AirQuality, ForecastSnapshot,
    │   │            PlaceConditions, WeatherInsight, ForecastQueries), repository (ForecastRepository),
    │   │            usecase (ObserveForecast, RefreshForecast, GetPlaceConditions),
    │   │            policy (WeatherConditionClassifier + WMO, InsightGenerator, FreshnessPolicy,
    │   │            UvBand, AirQualityBand, PressureTrend, CompassPoint)
    │   ├── data/    remote (Open-Meteo forecast + air quality API, DTO'lar, ForecastPayload),
    │   │            local (forecast_cache: Room + JSON), mapper, repository (önce önbellek), di
    │   └── presentation/
    │                detail (ForecastDetail Route/Screen/ViewModel/UiState; TemperatureDial, HourlyChart,
    │                        DailyRangeList, InstrumentPanel), myplaces, explore,
    │                model (ForecastUiMapper, PlaceRowMapper, ConditionUiRegistry), component (PlaceRow),
    │                di (ConditionUiModule: @IntoMap + özel @MapKey)
    ├── places/
    │   ├── domain/  RemovedPlace, SavedPlaceRepository, PlaceSearchRepository, LocationRepository,
    │   │            use case'ler (Observe/Toggle/Remove/Restore/Search/GetFeatured/LocateDevice)
    │   └── data/    local (saved_places + sıralı DAO işlemleri), remote (geocoding, PPL* süzgeci),
    │                catalog (TurkishCityCatalog), location (LocationManager, Geocoder), repository, di
    └── settings/
        ├── domain/  SettingsRepository, Observe/SetTemperatureUnit/SetWindSpeedUnit
        ├── data/    DataStoreSettingsRepository
        └── presentation/ Settings Route/Screen/ViewModel
```

Liste ekranları (Yerlerim, Keşfet) `forecast/presentation` altındadır. Bu ekranlar hava gösterir ve `places` özelliğini yalnızca domain üzerinden kullanır. Taslaktaki `weather` / `favorites` ayrımıyla aynı düzendir.

## Veri akışı

**Tahmin (önce önbellek):**

```
Open-Meteo ──fetch──▶ ForecastRemoteDataSource ──ForecastPayload──▶ RepositoryImpl ──doğrula (toDomain)──▶ Room (JSON)
                                                                                                          │
Screen ◀── StateFlow<ForecastDetailUiState> ◀── ViewModel ◀── ObserveForecastUseCase ◀── Flow<ForecastSnapshot?> ◀┘
```

- Ekran yalnızca önbelleği izler. Yenileme sadece önbelleğe yazar.
- Açılışta `FreshnessPolicy` 10 dakikadan eski veriyi arka planda tazeler. 45 dakikadan eski veri "bayat" gösterilir.
- Bozuk yanıt önce domain modeline çevrilerek doğrulanır; doğrulanamazsa önbelleğe hiç yazılmaz.
- Önbellekte bir haftadan eski kayıtlar yazma sırasında silinir.

**Liste satırları:**

- Tüm yerlerin anlık durumu ve bugünkü aralığı tek istekte gelir (çoklu koordinat).
- Ağ yoksa `ForecastRepository.getConditions`, önbellekteki tam tahminlerden satır üretir ve bunu `isFromCache` ile işaretler.

## Açık/Kapalı genişleme noktaları

| Senaryo | Eklenir | Değişir | Dokunulmaz |
|---|---|---|---|
| Başka bir hava sağlayıcısı | `ForecastRemoteDataSource` implementasyonu | `ForecastDataModule` (1 `@Binds`) | Domain, ViewModel'ler, ekranlar |
| Farklı öne çıkan şehirler | `FeaturedPlaceCatalog` implementasyonu | `PlacesDataModule` | Keşfet ekranı |
| Yeni hava görünümü | `ConditionUiModule`'e `@IntoMap` girdisi | — | Mapper, ekranlar |
| Yeni özet kuralı | `WeatherInsight` alt tipi + `InsightGenerator` kuralı + metin | `ForecastUiMapper.toUiText` | Ekran |
| Yeni birim | `WindSpeedUnit` değeri + metin | `UnitFormatter`, Ayarlar | Veri ve önbellek |
| Hata eşleme genel → ağ | `NetworkErrorMapper` | `CommonModule` (1 `@Binds`) | Repository'ler |
| Yeni tablo | Entity + DAO | `UfukDatabase` sürümü + `AutoMigration` | Mevcut veriler (şema dosyaları repoda) |

## Testler

- **Unit (79):**
  - Mimari kuralları.
  - Özet kuralları (her kural ayrı).
  - Kademeler, tazelik, birim dönüşümü.
  - MockWebServer ile API veri kaynakları, DTO → domain eşleme.
  - Önce önbellek repository'si (çevrimdışı senaryolar dahil).
  - Tüm ViewModel'ler (Turbine, sanal zamanla debounce), DataStore.
  - Konum hata ayrımı.
- **Cihaz (6):**
  - Tahmin önbelleği DAO'su.
  - Kayıtlı yerlerin sırası ve geri alma.
  - `MigrationTestHelper` ile 1→2 şema geçişi.
