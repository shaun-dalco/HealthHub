<template>
    <div class="app">
        <header class="topbar">
            <div class="brand">HealthHub</div>

            <nav class="nav">
                <button v-for="t in tabs"
                        :key="t.key"
                        class="navBtn"
                        :class="{ active: activeTab === t.key }"
                        @click="activeTab = t.key">
                    {{ t.label }}
                </button>
            </nav>
        </header>

        <main class="content">
            <component :is="currentComponent" />
        </main>
    </div>
</template>

<script setup>
    import { computed, ref } from "vue";

    import HealthView from "./views/HealthView.vue";
    import FitnessView from "./views/FitnessView.vue";
    import CaloriesView from "./views/CaloriesView.vue";

    const tabs = [
        { key: "health", label: "Health" },
        { key: "fitness", label: "Fitness" },
        { key: "calories", label: "Calories" },
    ];

    const activeTab = ref("health");

    const currentComponent = computed(() => {
        switch (activeTab.value) {
            case "fitness":
                return FitnessView;
            case "calories":
                return CaloriesView;
            case "health":
            default:
                return HealthView;
        }
    });
</script>