/* eslint-disable no-console */
/* global t n */

import Vue from 'vue'
import { loadState } from '@nextcloud/initial-state'
import App from './views/App.vue'

// Nextcloud l10n helpers
Vue.mixin({ methods: { t, n } })

function mountHealthHub() {
	const container = document.querySelector('#app-content #healthhub') || document.querySelector('#healthhub')

	if (!container) {
		// If this happens, something is wrong with index.php / DOM
		console.error('[HealthHub] #healthhub not found')
		return
	}

	let initialState
	try {
		initialState = loadState('healthhub', 'healthhub_initial_state')
		console.log('[HealthHub] Loaded initial state:', initialState)
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

	const VueApp = Vue.extend(App)

	new VueApp({
		propsData: {
			initialState,
		},
	}).$mount(container)
}

document.addEventListener('DOMContentLoaded', () => {
	mountHealthHub()
})
