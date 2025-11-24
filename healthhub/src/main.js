import { loadState } from '@nextcloud/initial-state'

function main() {
	console.log('[HealthHub] main() start')

	const container = document.querySelector('#app-content #healthhub')
	console.log('[HealthHub] container =', container)

	if (!container) {
		console.error('[HealthHub] #app-content #healthhub not found')
		return
	}

	let state
	try {
		state = loadState('healthhub', 'healthhub_initial_state')
		console.log('[HealthHub] Loaded initial state:', state)
	} catch (e) {
		console.error('[HealthHub] Failed to load initial state', e)
		container.innerHTML = `
			<div class="hh-section">
				<h2>HealthHub</h2>
				<p>Failed to load initial state <code>healthhub_initial_state</code>.</p>
			</div>
		`
		return
	}

	setupSidebarNavigation(state, container)

	setActiveSidebarEntry('overview')
	renderSection('overview', container, state)
}

function setupSidebarNavigation(state, container) {
	const entries = document.querySelectorAll('#app-navigation .app-navigation-entry')

	if (!entries.length) {
		console.warn('[HealthHub] No .app-navigation-entry elements found')
	}

	entries.forEach(entry => {
		const section = entry.dataset.section
		if (!section) {
			return
		}

		entry.addEventListener('click', (e) => {
			e.preventDefault()
			setActiveSidebarEntry(section)
			renderSection(section, container, state)
		})
	})
}

function setActiveSidebarEntry(section) {
	const entries = document.querySelectorAll('#app-navigation .app-navigation-entry')
	entries.forEach(entry => {
		if (entry.dataset.section === section) {
			entry.classList.add('active')
		} else {
			entry.classList.remove('active')
		}
	})
}

function renderSection(section, container, state) {
	switch (section) {
	case 'overview':
		renderOverview(container, state)
		break
	case 'heart-rate':
		renderHeartRate(container, state)
		break
	case 'steps':
		renderSteps(container, state)
		break
	case 'sleep':
		renderSleep(container, state)
		break
	case 'weight':
		renderWeight(container, state)
		break
	default:
		renderPlaceholder(container, section)
	}
}

function renderOverview(container, state) {
	const data = state.overview || {}
	container.innerHTML = `
		<div class="hh-section">
			<h2 class="hh-section-title">Overview</h2>
			<div class="hh-card-grid">
				<div class="hh-card">
					<div class="hh-card-label">Steps today</div>
					<div class="hh-card-value">${safeNumber(data.totalStepsToday, '-')}</div>
				</div>
				<div class="hh-card">
					<div class="hh-card-label">Avg heart rate</div>
					<div class="hh-card-value">${safeNumber(data.avgHeartRateToday, '-')} bpm</div>
				</div>
				<div class="hh-card">
					<div class="hh-card-label">Min / Max HR</div>
					<div class="hh-card-value">
						${safeNumber(data.minHeartRateToday, '-')} / ${safeNumber(data.maxHeartRateToday, '-')} bpm
					</div>
				</div>
				<div class="hh-card">
					<div class="hh-card-label">Sleep last night</div>
					<div class="hh-card-value">${safeNumber(data.sleepLastNightHours, '-')} h</div>
				</div>
				<div class="hh-card">
					<div class="hh-card-label">Weight</div>
					<div class="hh-card-value">${safeNumber(data.weightKg, '-')} kg</div>
				</div>
			</div>
			<div class="hh-updated-at">
				Last updated: ${data.updatedAt ? escapeHtml(data.updatedAt) : 'unknown'}
			</div>
		</div>
	`
}

function renderHeartRate(container, state) {
	const series = state.heartRate || []

	const rows = series.map(point => `
		<tr>
			<td>${escapeHtml(point.timestamp)}</td>
			<td>${safeNumber(point.bpm, '-')} bpm</td>
		</tr>
	`).join('')

	container.innerHTML = `
		<div class="hh-section">
			<h2 class="hh-section-title">Heart rate</h2>
			<table class="hh-table">
				<thead>
					<tr>
						<th>Time</th>
						<th>BPM</th>
					</tr>
				</thead>
				<tbody>
					${rows || '<tr><td colspan="2">No data</td></tr>'}
				</tbody>
			</table>
		</div>
	`
}

function renderSteps(container, state) {
	const days = state.steps || []

	const rows = days.map(day => `
		<tr>
			<td>${escapeHtml(day.date)}</td>
			<td>${safeNumber(day.count, '-')}</td>
		</tr>
	`).join('')

	container.innerHTML = `
		<div class="hh-section">
			<h2 class="hh-section-title">Steps</h2>
			<table class="hh-table">
				<thead>
					<tr>
						<th>Date</th>
						<th>Steps</th>
					</tr>
				</thead>
				<tbody>
					${rows || '<tr><td colspan="2">No data</td></tr>'}
				</tbody>
			</table>
		</div>
	`
}

function renderSleep(container, state) {
	const nights = state.sleep || []

	const rows = nights.map(night => `
		<tr>
			<td>${escapeHtml(night.date)}</td>
			<td>${safeNumber(night.durationHours, '-')} h</td>
			<td>${safeNumber(night.qualityScore, '-')}</td>
		</tr>
	`).join('')

	container.innerHTML = `
		<div class="hh-section">
			<h2 class="hh-section-title">Sleep</h2>
			<table class="hh-table">
				<thead>
					<tr>
						<th>Date</th>
						<th>Duration</th>
						<th>Quality</th>
					</tr>
				</thead>
				<tbody>
					${rows || '<tr><td colspan="3">No data</td></tr>'}
				</tbody>
			</table>
		</div>
	`
}

function renderWeight(container, state) {
	const entries = state.weight || []

	const rows = entries.map(entry => `
		<tr>
			<td>${escapeHtml(entry.date)}</td>
			<td>${safeNumber(entry.kg, '-')} kg</td>
		</tr>
	`).join('')

	container.innerHTML = `
		<div class="hh-section">
			<h2 class="hh-section-title">Weight</h2>
			<table class="hh-table">
				<thead>
					<tr>
						<th>Date</th>
						<th>Weight</th>
					</tr>
				</thead>
				<tbody>
					${rows || '<tr><td colspan="2">No data</td></tr>'}
				</tbody>
			</table>
		</div>
	`
}

function renderPlaceholder(container, section) {
	container.innerHTML = `
		<div class="hh-section">
			<h2 class="hh-section-title">Section: ${escapeHtml(section)}</h2>
			<p>No renderer implemented yet for this section.</p>
		</div>
	`
}

function safeNumber(value, fallback) {
	return (value === null || value === undefined || Number.isNaN(value))
		? fallback
		: value
}

function escapeHtml(str) {
	if (str === null || str === undefined) {
		return ''
	}
	return String(str)
		.replace(/&/g, '&amp;')
		.replace(/</g, '&lt;')
		.replace(/>/g, '&gt;')
		.replace(/"/g, '&quot;')
		.replace(/'/g, '&#039;')
}

document.addEventListener('DOMContentLoaded', () => {
	console.log('[HealthHub] DOMContentLoaded')
	main()
})
