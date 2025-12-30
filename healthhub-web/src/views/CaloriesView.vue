<template>
    <section class="page">
        <h1>Calories</h1>

        <!-- Date + reload -->
        <div class="topBar">
            <label class="dateLabel">
                Date:
                <input class="dateInput" type="date" v-model="day" @change="loadDay()" />
            </label>

            <button class="btn" type="button" @click="loadDay()" :disabled="loading">
                {{ loading ? "Loading..." : "Reload" }}
            </button>

            <span v-if="msg" class="msg">{{ msg }}</span>
        </div>

        <div class="card">
            <div class="cardTitle">Daily macros</div>

            <div class="pieWrap">
                <Pie :data="macroPieData" :options="macroPieOptions" />
            </div>

            <div class="macroNumbers">
                <div><strong>Carbs:</strong> {{ fmt(macroTotals.carbs) }} g</div>
                <div><strong>Protein:</strong> {{ fmt(macroTotals.protein) }} g</div>
                <div><strong>Fat:</strong> {{ fmt(macroTotals.fats) }} g</div>
            </div>
        </div>


        <!-- Day totals -->
        <div class="card">
            <div class="cardTitle">Day totals</div>
            <div class="totalsGrid">
                <div><strong>Calories:</strong> {{ totals.calories }}</div>
                <div><strong>Protein:</strong> {{ totals.protein }} g</div>
                <div><strong>Carbs:</strong> {{ totals.carbs }} g</div>
                <div><strong>Fats:</strong> {{ totals.fats }} g</div>
                <div><strong>Sugar:</strong> {{ totals.sugar }} g</div>
                <div><strong>Sat fat:</strong> {{ totals.saturated_fat }} g</div>
                <div><strong>Sodium:</strong> {{ totals.sodium }} mg</div>
            </div>
        </div>

        <!-- Meal navigation -->
        <div class="mealNav">
            <button class="navBtn" type="button" @click="prevMeal()" aria-label="Previous meal">
                <
            </button>

            <div class="mealTitleWrap">
                <div class="mealTitle">{{ prettyMeal(currentMeal) }}</div>
                <div class="mealSub">
                    {{ mealTotals(currentMeal).calories }} cal
                    P {{ mealTotals(currentMeal).protein }}
                    C {{ mealTotals(currentMeal).carbs }}
                    F {{ mealTotals(currentMeal).fats }}
                </div>
            </div>

            <button class="navBtn" type="button" @click="nextMeal()" aria-label="Next meal">
                >
            </button>
        </div>

        <!-- Meal content (single view) -->
        <div class="card">
            <div class="cardHeader">
                <button class="btn" type="button" @click="openAddModal()">
                    + Add entry
                </button>
            </div>

            <div v-if="meals[currentMeal].length === 0" class="empty">
                No foods yet for {{ prettyMeal(currentMeal) }}.
            </div>

            <div v-else class="foodsList">
                <div class="foodRow" v-for="f in meals[currentMeal]" :key="f.id">
                    <div class="foodMain">
                        <div class="foodName">{{ f.name }}</div>
                        <div class="foodMacros">
                            {{ fmt(f.calories) }} cal
                            P {{ fmt(f.protein) }}
                            C {{ fmt(f.carbs) }}
                            F {{ fmt(f.fats) }}
                            Sugar {{ fmt(f.sugar) }}
                            Sat {{ fmt(f.saturated_fat) }}
                            Na {{ fmt(f.sodium) }}
                        </div>
                    </div>

                    <button class="removeBtn" type="button" @click="removeFood(f.id)" :disabled="removingId === f.id">
                        {{ removingId === f.id ? "..." : "X" }}
                    </button>
                </div>
            </div>
        </div>

        <!-- ADD MODAL -->
        <div v-if="showAddModal" class="modalOverlay" @click.self="closeAddModal()">
            <div class="modal">
                <div class="modalHeader">
                    <div class="modalTitle">Add food to {{ prettyMeal(currentMeal) }}</div>
                    <button class="iconBtn" type="button" @click="closeAddModal()" aria-label="Close">X</button>
                </div>

                <div class="modalBody">
                    <label class="field">
                        <div class="fieldLabel">Food name</div>
                        <input class="textInput" v-model="draft.name" placeholder="e.g. Oats" />
                    </label>

                    <div class="grid">
                        <label class="field">
                            <div class="fieldLabel">Calories</div>
                            <input class="numInput" type="number" v-model.number="draft.calories" />
                        </label>

                        <label class="field">
                            <div class="fieldLabel">Protein (g)</div>
                            <input class="numInput" type="number" v-model.number="draft.protein" />
                        </label>

                        <label class="field">
                            <div class="fieldLabel">Carbs (g)</div>
                            <input class="numInput" type="number" v-model.number="draft.carbs" />
                        </label>

                        <label class="field">
                            <div class="fieldLabel">Fats (g)</div>
                            <input class="numInput" type="number" v-model.number="draft.fats" />
                        </label>

                        <label class="field">
                            <div class="fieldLabel">Sugar (g)</div>
                            <input class="numInput" type="number" v-model.number="draft.sugar" />
                        </label>

                        <label class="field">
                            <div class="fieldLabel">Sat fat (g)</div>
                            <input class="numInput" type="number" v-model.number="draft.saturated_fat" />
                        </label>

                        <label class="field">
                            <div class="fieldLabel">Sodium (mg)</div>
                            <input class="numInput" type="number" v-model.number="draft.sodium" />
                        </label>
                    </div>
                </div>

                <div class="modalFooter">
                    <button class="btn" type="button" @click="closeAddModal()" :disabled="adding">
                        Cancel
                    </button>
                    <button class="btn primary" type="button" @click="confirmAdd()" :disabled="adding || !draft.name.trim()">
                        {{ adding ? "Adding..." : "Add" }}
                    </button>
                </div>

                <div v-if="modalMsg" class="modalMsg">{{ modalMsg }}</div>
            </div>
        </div>
    </section>
</template>

<script setup lang="ts">
    import { computed, onMounted, reactive, ref } from "vue";
    import { Pie } from "vue-chartjs";
    import { Chart as ChartJS, ArcElement, Tooltip, Legend } from "chart.js";

    ChartJS.register(ArcElement, Tooltip, Legend);

    // -------- Pie chart ----------
    const macroTotals = computed(() => {
        let protein = 0, carbs = 0, fats = 0;

        for (const mt of mealOrder) {
            for (const f of meals[mt]) {
                protein += num(f.protein);
                carbs += num(f.carbs);
                fats += num(f.fats);
            }
        }

        return { protein, carbs, fats };
    });

    const macroPieData = computed(() => ({
        labels: ["Carbs", "Protein", "Fat"],
        datasets: [
            {
                data: [
                    macroTotals.value.carbs,
                    macroTotals.value.protein,
                    macroTotals.value.fats
                ],
                backgroundColor: [
                    "#4da6ff", // Carbs (blue)
                    "#66cc99", // Protein (green)
                    "#ff9966", // Fat (orange)
                ],
                borderColor: "#ffffff",
                borderWidth: 2,
            },
        ],
    }));


    const macroPieOptions = {
        responsive: true,
        maintainAspectRatio: false,
    };



    type MealType = "breakfast" | "lunch" | "dinner" | "snacks";

    type Food = {
        id: number;
        name: string;
        calories: number;
        protein: number;
        carbs: number;
        fats: number;
        sugar: number;
        saturated_fat: number;
        sodium: number;
    };

    type DayResponse = {
        day: string;
        meals: Record<MealType, Food[]>;
    };

    const API = "http://localhost:3001/api/calories";

    const mealOrder: MealType[] = ["breakfast", "lunch", "dinner", "snacks"];
    const currentMealIndex = ref(0);
    const currentMeal = computed(() => mealOrder[currentMealIndex.value]);

    function todayISO() {
        const d = new Date();
        const yyyy = d.getFullYear();
        const mm = String(d.getMonth() + 1).padStart(2, "0");
        const dd = String(d.getDate()).padStart(2, "0");
        return `${yyyy}-${mm}-${dd}`;
    }

    const day = ref(todayISO());
    const loading = ref(false);
    const msg = ref("");

    const removingId = ref<number | null>(null);

    // Modal state
    const showAddModal = ref(false);
    const adding = ref(false);
    const modalMsg = ref("");

    const meals = reactive<Record<MealType, Food[]>>({
        breakfast: [],
        lunch: [],
        dinner: [],
        snacks: [],
    });

    const draft = reactive({
        name: "",
        calories: 0,
        protein: 0,
        carbs: 0,
        fats: 0,
        sugar: 0,
        saturated_fat: 0,
        sodium: 0,
    });

    function prettyMeal(m: MealType) {
        return m.charAt(0).toUpperCase() + m.slice(1);
    }

    function fmt(n: number) {
        if (!Number.isFinite(n)) return "0";
        const isInt = Math.abs(n - Math.round(n)) < 1e-9;
        return isInt ? String(Math.round(n)) : n.toFixed(1);
    }

    function num(v: any) {
        const n = Number(v);
        return Number.isFinite(n) ? n : 0;
    }

    function prevMeal() {
        currentMealIndex.value = (currentMealIndex.value - 1 + mealOrder.length) % mealOrder.length;
    }

    function nextMeal() {
        currentMealIndex.value = (currentMealIndex.value + 1) % mealOrder.length;
    }

    async function loadDay() {
        loading.value = true;
        msg.value = "";

        try {
            const res = await fetch(`${API}/day/${day.value}`);
            if (!res.ok) throw new Error(`Load failed (${res.status})`);

            const data: DayResponse = await res.json();

            for (const mt of mealOrder) {
                meals[mt] = (data.meals?.[mt] ?? []).map((f) => ({
                    ...f,
                    calories: num(f.calories),
                    protein: num(f.protein),
                    carbs: num(f.carbs),
                    fats: num(f.fats),
                    sugar: num(f.sugar),
                    saturated_fat: num(f.saturated_fat),
                    sodium: num(f.sodium),
                }));
            }
        } catch (e: any) {
            msg.value = e?.message ? String(e.message) : "Failed to load";
        } finally {
            loading.value = false;
        }
    }

    function openAddModal() {
        modalMsg.value = "";
        draft.name = "";
        draft.calories = 0;
        draft.protein = 0;
        draft.carbs = 0;
        draft.fats = 0;
        draft.sugar = 0;
        draft.saturated_fat = 0;
        draft.sodium = 0;
        showAddModal.value = true;
    }

    function closeAddModal() {
        if (adding.value) return;
        showAddModal.value = false;
    }

    async function confirmAdd() {
        adding.value = true;
        modalMsg.value = "";

        const payload = {
            mealType: currentMeal.value,
            name: draft.name.trim(),
            calories: num(draft.calories),
            protein: num(draft.protein),
            carbs: num(draft.carbs),
            fats: num(draft.fats),
            sugar: num(draft.sugar),
            saturated_fat: num(draft.saturated_fat),
            sodium: num(draft.sodium),
        };

        try {
            const res = await fetch(`${API}/day/${day.value}/food`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload),
            });

            if (!res.ok) {
                const t = await res.text().catch(() => "");
                throw new Error(`Add failed (${res.status}) ${t}`);
            }

            await loadDay();
            showAddModal.value = false;
        } catch (e: any) {
            modalMsg.value = e?.message ? String(e.message) : "Failed to add";
        } finally {
            adding.value = false;
        }
    }

    async function removeFood(id: number) {
        removingId.value = id;
        msg.value = "";

        try {
            const res = await fetch(`${API}/food/${id}`, { method: "DELETE" });
            if (!res.ok) {
                const t = await res.text().catch(() => "");
                throw new Error(`Delete failed (${res.status}) ${t}`);
            }
            await loadDay();
        } catch (e: any) {
            msg.value = e?.message ? String(e.message) : "Failed to delete";
        } finally {
            removingId.value = null;
        }
    }

    const totals = computed(() => {
        const sum = {
            calories: 0,
            protein: 0,
            carbs: 0,
            fats: 0,
            sugar: 0,
            saturated_fat: 0,
            sodium: 0,
        };

        for (const mt of mealOrder) {
            for (const f of meals[mt]) {
                sum.calories += num(f.calories);
                sum.protein += num(f.protein);
                sum.carbs += num(f.carbs);
                sum.fats += num(f.fats);
                sum.sugar += num(f.sugar);
                sum.saturated_fat += num(f.saturated_fat);
                sum.sodium += num(f.sodium);
            }
        }

        return {
            calories: fmt(sum.calories),
            protein: fmt(sum.protein),
            carbs: fmt(sum.carbs),
            fats: fmt(sum.fats),
            sugar: fmt(sum.sugar),
            saturated_fat: fmt(sum.saturated_fat),
            sodium: fmt(sum.sodium),
        };
    });

    function mealTotals(mt: MealType) {
        let calories = 0, protein = 0, carbs = 0, fats = 0;

        for (const f of meals[mt]) {
            calories += num(f.calories);
            protein += num(f.protein);
            carbs += num(f.carbs);
            fats += num(f.fats);
        }

        return { calories: fmt(calories), protein: fmt(protein), carbs: fmt(carbs), fats: fmt(fats) };
    }

    onMounted(loadDay);
</script>

<style scoped>
    .topBar {
        display: flex;
        align-items: center;
        gap: 10px;
        flex-wrap: wrap;
        margin-bottom: 12px;
    }

    .dateLabel {
        display: flex;
        align-items: center;
        gap: 8px;
    }

    .dateInput {
        border: 1px solid #ddd;
        border-radius: 10px;
        padding: 8px 10px;
    }

    .btn {
        border: 1px solid #ddd;
        border-radius: 12px;
        padding: 8px 12px;
        cursor: pointer;
    }

        .btn.primary {
            border-color: #bbb;
            font-weight: 700;
        }

    .msg {
        color: #c00;
    }

    .card {
        border: 1px solid #eee;
        border-radius: 14px;
        padding: 12px;
        margin-bottom: 14px;
    }

    .cardTitle {
        font-weight: 700;
        margin-bottom: 8px;
    }

    .totalsGrid {
        display: grid;
        grid-template-columns: repeat(2, minmax(220px, 1fr));
        gap: 6px 14px;
    }

    .mealNav {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 12px;
        margin: 10px 0 14px;
    }

    .navBtn {
        border: 1px solid #ddd;
        border-radius: 999px;
        padding: 8px 12px;
        cursor: pointer;
        min-width: 44px;
    }

    .mealTitleWrap {
        text-align: center;
        min-width: 220px;
    }

    .mealTitle {
        font-weight: 800;
        font-size: 18px;
    }

    .mealSub {
        opacity: 0.75;
        font-size: 13px;
        margin-top: 2px;
    }

    .cardHeader {
        display: flex;
        justify-content: center;
        margin-bottom: 10px;
    }

    .empty {
        opacity: 0.7;
        padding: 10px 2px;
        text-align: center;
    }

    .foodsList {
        display: grid;
        gap: 8px;
    }

    .foodRow {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 10px;
        border: 1px solid #f0f0f0;
        border-radius: 12px;
        padding: 10px;
    }

    .foodName {
        font-weight: 800;
    }

    .foodMacros {
        opacity: 0.8;
        font-size: 13px;
        margin-top: 3px;
    }

    .removeBtn {
        border: 1px solid #ddd;
        border-radius: 12px;
        padding: 6px 10px;
        cursor: pointer;
    }

    /* Modal */
    .modalOverlay {
        position: fixed;
        inset: 0;
        background: rgba(0, 0, 0, 0.35);
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 16px;
        z-index: 9999;
    }

    .modal {
        width: min(820px, 100%);
        background: #fff;
        border-radius: 16px;
        border: 1px solid #eee;
        padding: 12px;
    }

    .modalHeader {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 10px;
        padding-bottom: 8px;
        border-bottom: 1px solid #f1f1f1;
    }

    .modalTitle {
        font-weight: 800;
        font-size: 16px;
    }

    .iconBtn {
        border: 1px solid #ddd;
        border-radius: 12px;
        padding: 6px 10px;
        cursor: pointer;
    }

    .modalBody {
        padding-top: 10px;
    }

    .field {
        display: grid;
        gap: 6px;
        margin-bottom: 10px;
    }

    .fieldLabel {
        font-size: 12px;
        opacity: 0.8;
    }

    .textInput,
    .numInput {
        border: 1px solid #ddd;
        border-radius: 10px;
        padding: 8px 10px;
    }

    .grid {
        display: grid;
        grid-template-columns: repeat(2, minmax(180px, 1fr));
        gap: 10px;
    }

    .modalFooter {
        display: flex;
        justify-content: flex-end;
        gap: 10px;
        margin-top: 12px;
    }

    .modalMsg {
        margin-top: 10px;
        color: #c00;
    }

    .pieWrap {
        height: 220px;
        margin-top: 8px;
    }

    .macroNumbers {
        display: flex;
        gap: 14px;
        flex-wrap: wrap;
        margin-top: 10px;
        opacity: 0.9;
    }

</style>
