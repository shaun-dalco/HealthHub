<template>
	<!-- Outer content area; keep id to match your CSS -->
	<div id="healthhub-main-panel">
		<!-- OVERVIEW -->
		<section v-if="activeSection === 'overview'" class="hh-section">
			<h2 class="hh-section-title">
				Overview
			</h2>

			<div class="hh-card-grid">
				<div class="hh-card">
					<div class="hh-card-label">
						Steps today
					</div>
					<div class="hh-card-value">
						{{ safeNumber(overview.totalStepsToday, '-') }}
					</div>
				</div>

				<div class="hh-card">
					<div class="hh-card-label">
						Avg heart rate
					</div>
					<div class="hh-card-value">
						{{ safeNumber(overview.avgHeartRateToday, '-') }} bpm
					</div>
				</div>

				<div class="hh-card">
					<div class="hh-card-label">
						Min / Max HR
					</div>
					<div class="hh-card-value">
						{{ safeNumber(overview.minHeartRateToday, '-') }}
						/
						{{ safeNumber(overview.maxHeartRateToday, '-') }} bpm
					</div>
				</div>

				<div class="hh-card">
					<div class="hh-card-label">
						Sleep last night
					</div>
					<div class="hh-card-value">
						{{ safeNumber(overview.sleepLastNightHours, '-') }} h
					</div>
				</div>

				<div class="hh-card">
					<div class="hh-card-label">
						Weight
					</div>
					<div class="hh-card-value">
						{{ safeNumber(overview.weightKg, '-') }} kg
					</div>
				</div>
			</div>

			<div class="hh-updated-at">
				Last updated:
				{{ overview.updatedAt || 'unknown' }}
			</div>
		</section>

		<!-- HEART RATE -->
		<section v-else-if="activeSection === 'heart-rate'" class="hh-section">
			<h2 class="hh-section-title">
				Heart rate
			</h2>
			<table class="hh-table">
				<thead>
					<tr>
						<th>
							Time
						</th>
						<th>
							BPM
						</th>
					</tr>
				</thead>
				<tbody>
					<tr v-if="heartRate.length === 0">
						<td colspan="2">
							No data
						</td>
					</tr>
					<tr v-for="point in heartRate" :key="point.timestamp">
						<td>
							{{ point.timestamp }}
						</td>
						<td>
							{{ safeNumber(point.bpm, '-') }} bpm
						</td>
					</tr>
				</tbody>
			</table>
		</section>

		<!-- STEPS -->
		<section v-else-if="activeSection === 'steps'" class="hh-section">
			<h2 class="hh-section-title">
				Steps
			</h2>
			<table class="hh-table">
				<thead>
					<tr>
						<th>
							Date
						</th>
						<th>
							Steps
						</th>
					</tr>
				</thead>
				<tbody>
					<tr v-if="steps.length === 0">
						<td colspan="2">
							No data
						</td>
					</tr>
					<tr v-for="day in steps" :key="day.date">
						<td>
							{{ day.date }}
						</td>
						<td>
							{{ safeNumber(day.count, '-') }}
						</td>
					</tr>
				</tbody>
			</table>
		</section>

		<!-- SLEEP -->
		<section v-else-if="activeSection === 'sleep'" class="hh-section">
			<h2 class="hh-section-title">
				Sleep
			</h2>
			<table class="hh-table">
				<thead>
					<tr>
						<th>
							Date
						</th>
						<th>
							Duration
						</th>
						<th>
							Quality
						</th>
					</tr>
				</thead>
				<tbody>
					<tr v-if="sleep.length === 0">
						<td colspan="3">
							No data
						</td>
					</tr>
					<tr v-for="night in sleep" :key="night.date">
						<td>
							{{ night.date }}
						</td>
						<td>
							{{ safeNumber(night.durationHours, '-') }} h
						</td>
						<td>
							{{ safeNumber(night.qualityScore, '-') }}
						</td>
					</tr>
				</tbody>
			</table>
		</section>

		<!-- WEIGHT -->
		<section v-else-if="activeSection === 'weight'" class="hh-section">
			<h2 class="hh-section-title">
				Weight
			</h2>
			<table class="hh-table">
				<thead>
					<tr>
						<th>
							Date
						</th>
						<th>
							Weight
						</th>
					</tr>
				</thead>
				<tbody>
					<tr v-if="weight.length === 0">
						<td colspan="2">
							No data
						</td>
					</tr>
					<tr v-for="entry in weight" :key="entry.date">
						<td>
							{{ entry.date }}
						</td>
						<td>
							{{ safeNumber(entry.kg, '-') }} kg
						</td>
					</tr>
				</tbody>
			</table>
		</section>

		<!-- FALLBACK -->
		<section v-else class="hh-section">
			<h2 class="hh-section-title">
				Section: {{ activeSection }}
			</h2>
			<p>
				No renderer implemented yet for this section.
			</p>
		</section>
	</div>
</template>

<script>
export default {
	name: 'MyMainContent',

	props: {
		initialState: {
			type: Object,
			required: true,
		},
		activeSection: {
			type: String,
			required: true,
		},
	},

	computed: {
		overview() {
			return this.initialState.overview || {}
		},
		heartRate() {
			return this.initialState.heartRate || []
		},
		steps() {
			return this.initialState.steps || []
		},
		sleep() {
			return this.initialState.sleep || []
		},
		weight() {
			return this.initialState.weight || []
		},
	},

	methods: {
		safeNumber(value, fallback = '-') {
			return (value === null || value === undefined || Number.isNaN(value))
				? fallback
				: value
		},
	},
}
</script>
