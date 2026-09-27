package com.kampplus.ufuk.testing

import com.kampplus.ufuk.feature.forecast.domain.policy.InsightGenerator
import com.kampplus.ufuk.feature.forecast.domain.policy.WmoWeatherConditionClassifier
import com.kampplus.ufuk.feature.forecast.presentation.model.ConditionUiRegistry
import com.kampplus.ufuk.feature.forecast.presentation.model.ForecastUiMapper

val testConditions = ConditionUiRegistry(classifier = WmoWeatherConditionClassifier(), entries = emptyMap())

fun testForecastUiMapper() = ForecastUiMapper(
    conditions = testConditions,
    insightGenerator = InsightGenerator(WmoWeatherConditionClassifier())
)
