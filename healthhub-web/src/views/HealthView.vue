<template>
    <section class="page">
        <h1>Steps</h1>

        <div class="chartWrap">
            <Line :data="chartData" :options="options" :key="chartKey" />
        </div>

        <!-- Editable rows -->
        <div class="simpleList">
            <div class="row" v-for="(r, i) in rows" :key="i">
                <input class="labelInput" v-model="r.day" placeholder="YYYY-MM-DD" />
                <span class="colon">:</span>
                <input class="valueInput" type="number" v-model.number="r.steps" placeholder="0" />
                <button class="removeBtn" type="button" @click="removeRow(i)">X</button>
            </div>

            <div class="actions">
                <button class="addBtn" type="button" @click="addRow()">+ Add</button>
                <button class="saveBtn" type="button" @click="save()" :disabled="saving">
                    {{ saving ? "Saving..." : "Save" }}
                </button>
                <span v-if="savedMsg" class="savedMsg">{{ savedMsg }}</span>
            </div>
        </div>

        <hr />

        <h1>Heart Rate</h1>
        <p>Stub. This will later include: steps, heart rate, sleep, etc.</p>
    </section>
</template>

<script setup lang="ts">
    import { computed, onMounted, ref } from "vue";
    import { Line } from "vue-chartjs";
    import {
        Chart as ChartJS,
        CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip,
    } from "chart.js";

    ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip);

    type StepRow = { day: string; steps: number };

    const rows = ref<StepRow[]>([]);
    const saving = ref(false);
    const savedMsg = ref("");
    const chartKey = ref(0);

    const options = { responsive: true, maintainAspectRatio: false };

    const chartData = computed(() => ({
        labels: rows.value.map(r => r.day),
        datasets: [{
            label: "Steps",
            data: rows.value.map(r => r.steps),
            borderColor: "#f87979"
        }],
    }));

    async function load() {
        const res = await fetch("http://localhost:3001/api/steps");
        const data: StepRow[] = await res.json();
        rows.value = data;
        chartKey.value++;
    }

    function addRow() {
        rows.value.push({ day: "2025-12-01", steps: 0 });
        chartKey.value++;
    }

    function removeRow(i: number) {
        rows.value.splice(i, 1);
        chartKey.value++;
    }

    async function save() {
        saving.value = true;
        savedMsg.value = "";

        // Optional: sort before saving so chart/list are stable
        const payload = [...rows.value].sort((a, b) => a.day.localeCompare(b.day));

        const res = await fetch("http://localhost:3001/api/steps/bulk", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
        });

        saving.value = false;

        if (!res.ok) {
            savedMsg.value = "Save failed";
            return;
        }

        savedMsg.value = "Saved";
        await load(); // reload from DB so you know it’s persisted
    }

    onMounted(load);
</script>