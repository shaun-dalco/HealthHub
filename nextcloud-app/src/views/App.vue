<template>
	<NcContent app-name="healthhub">
		<!-- Left sidebar handled by @nextcloud/vue inside MyNavigation -->
		<MyNavigation
			:active-section="activeSection"
			@section-change="handleSectionChange" />

		<!-- Main content area, Nextcloud handles width/height -->
		<NcAppContent>
			<!-- Add padded wrapper here -->
			<div class="healthhub-content">
				<MyMainContent
					:initial-state="initialState"
					:active-section="activeSection" />
			</div>
		</NcAppContent>
	</NcContent>
</template>

<script>
import { NcContent, NcAppContent } from '@nextcloud/vue'
import MyNavigation from '../components/MyNavigation.vue'
import MyMainContent from '../components/MyMainContent.vue'

export default {
	name: 'App',

	components: {
		NcContent,
		NcAppContent,
		MyNavigation,
		MyMainContent,
	},

	props: {
		initialState: {
			type: Object,
			required: true,
		},
	},

	data() {
		return {
			activeSection: 'overview',
		}
	},

	methods: {
		handleSectionChange(section) {
			this.activeSection = section
		},
	},
}
</script>

<style scoped>
.healthhub-content {
	padding: 24px;
	box-sizing: border-box;
	width: 100%;
	height: 100%;
}
</style>
