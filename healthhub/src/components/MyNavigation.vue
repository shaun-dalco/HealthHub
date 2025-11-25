<template>
	<NcAppNavigation>
		<template #list>
			<NcAppNavigationItem
				v-for="item in items"
				:key="item.id"
				:name="item.label"
				:class="{ 'is-active': activeSection === item.id }"
				@click="selectSection(item.id)">
				<template #icon>
					<span :class="item.iconClass" />
				</template>
			</NcAppNavigationItem>
		</template>
	</NcAppNavigation>
</template>

<script>
import { NcAppNavigation, NcAppNavigationItem } from '@nextcloud/vue'

export default {
	name: 'MyNavigation',

	components: {
		NcAppNavigation,
		NcAppNavigationItem,
	},

	props: {
		activeSection: {
			type: String,
			required: true,
		},
	},

	data() {
		return {
			items: [
				{ id: 'overview', label: 'Overview', iconClass: 'icon-info' },
				{ id: 'heart-rate', label: 'Heart rate', iconClass: 'icon-favorite' },
				{ id: 'steps', label: 'Steps', iconClass: 'icon-home' },
				{ id: 'sleep', label: 'Sleep', iconClass: 'icon-calendar' },
				{ id: 'weight', label: 'Weight', iconClass: 'icon-contacts' },
			],
		}
	},

	methods: {
		selectSection(id) {
			this.$emit('section-change', id)
		},
	},
}
</script>

<style scoped>
.is-active > a,
.is-active {
	font-weight: 600;
}
</style>
