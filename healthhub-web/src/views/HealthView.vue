<template>
    <section class="page">
        <h1>Steps</h1>

        <div class="chartWrap">
            <Line :data="chartData" :options="options" :key="chartKey" />
        </div>

        <!-- Editable rows -->
        <button class="toggleBtn" @click="showSteps = !showSteps">
            Edit Steps
        </button>
        <div class="simpleList" v-show="showSteps">
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
        <div class="chartWrap">
            <Line :data="chartDataHR" :options="options" :key="chartKeyHR" />
        </div>

        <!-- Editable rows (Heart Rate) -->
        <button class="toggleBtn" @click="showHR = !showHR">
            Edit HR
        </button>
        <div class="simpleList" v-show="showHR">
            <div class="row" v-for="(r, i) in hrRows" :key="i">
                <input class="labelInput" v-model="r.day" placeholder="YYYY-MM-DD" />
                <span class="colon">:</span>
                <input class="valueInput" type="number" v-model.number="r.hr" placeholder="0" />
                <button class="removeBtn" type="button" @click="removeHrRow(i)">X</button>
            </div>

            <div class="actions">
                <button class="addBtn" type="button" @click="addHrRow()">+ Add</button>
                <button class="saveBtn" type="button" @click="saveHeartRate()" :disabled="savingHR">
                    {{ savingHR ? "Saving..." : "Save" }}
                </button>
                <span v-if="savedMsgHR" class="savedMsg">{{ savedMsgHR }}</span>
            </div>
        </div>

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

    const showSteps = ref(false)
    const showHR = ref(false)

    // ----- STEPS -----
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

    // ----- Heart Rate -----
    type HeartRateRow = { day: string; hr: number };

    const hrRows = ref<HeartRateRow[]>([]);
    const savingHR = ref(false);
    const savedMsgHR = ref("");
    const chartKeyHR = ref(0);

    const chartDataHR = computed(() => ({
        labels: hrRows.value.map((r) => r.day),
        datasets: [
            {
                label: "Avg BPM",
                data: hrRows.value.map((r) => r.hr),
                borderColor: "#79a7f8",
            },
        ],
    }));

    async function loadHeartRate() {
        savedMsgHR.value = "";
        const res = await fetch("http://localhost:3001/api/heartrate");
        if (!res.ok) {
            savedMsgHR.value = "Failed to load heart rate";
            return;
        }
        const data: HeartRateRow[] = await res.json();
        hrRows.value = data;
        chartKeyHR.value++;
    }

    function addHrRow() {
        hrRows.value.push({ day: "2025-12-01", hr: 0 });
        chartKeyHR.value++;
    }

    function removeHrRow(i: number) {
        hrRows.value.splice(i, 1);
        chartKeyHR.value++;
    }

    async function saveHeartRate() {
        savingHR.value = true;
        savedMsgHR.value = "";

        const payload = [...hrRows.value].sort((a, b) => a.day.localeCompare(b.day));

        const res = await fetch("http://localhost:3001/api/heartrate/bulk", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
        });

        savingHR.value = false;

        if (!res.ok) {
            savedMsgHR.value = "Save failed";
            return;
        }

        savedMsgHR.value = "Saved";
        await loadHeartRate(); // reload from DB
    }

    onMounted(async () => {
        await load()
        await loadHeartRate()
    })

</script>