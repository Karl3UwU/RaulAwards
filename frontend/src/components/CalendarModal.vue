<template>
  <div v-if="show" class="overlay" @click.self="$emit('close')">
    <div class="modal">
      <div class="modal-header">
        <button @click="prevMonth">‹</button>
        <h3>{{ monthYearLabel }}</h3>
        <button @click="nextMonth">›</button>
      </div>
      <div class="weekdays">
        <span v-for="d in ['Sun','Mon','Tue','Wed','Thu','Fri','Sat']" :key="d">{{ d }}</span>
      </div>
      <div class="grid">
        <button
          v-for="day in days"
          :key="day.key"
          class="cell"
          :class="day.classes"
          :disabled="!day.isSunday || !day.isClickable"
          @click="onSelect(day)"
        >
          {{ day.date.getDate() }}
        </button>
      </div>
      <div class="legend">
        <span class="legend-item today">Today</span>
        <span class="legend-item closest">Closest past Sunday</span>
        <span class="legend-item has-entry">Sunday with entries</span>
        <span class="legend-item none">No entry</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CalendarModal',
  props: {
    show: { type: Boolean, default: false },
    archive: { type: Array, default: () => [] } // [{sundayDate, overall, raul}]
  },
  data() {
    const now = new Date()
    return {
      viewYear: now.getFullYear(),
      viewMonth: now.getMonth()
    }
  },
  watch: {
    show(newVal) {
      if (newVal) {
        // Reset to current month when opening
        const now = new Date()
        this.viewYear = now.getFullYear()
        this.viewMonth = now.getMonth()
      }
    }
  },
  computed: {
    monthYearLabel() {
      const d = new Date(this.viewYear, this.viewMonth, 1)
      return d.toLocaleDateString('en-US', { month: 'long', year: 'numeric' })
    },
    archiveSet() {
      const set = new Set()
      this.archive.forEach(r => {
        if (r.overall || r.raul) set.add(r.sundayDate)
      })
      return set
    },
    closestPastSunday() {
      const today = new Date()
      const d = new Date(today)
      d.setDate(d.getDate() - ((d.getDay() + 7 - 0) % 7))
      return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
    },
    todayIso() {
      const today = new Date()
      return today.getFullYear() + '-' + String(today.getMonth() + 1).padStart(2, '0') + '-' + String(today.getDate()).padStart(2, '0')
    },
    days() {
      const first = new Date(this.viewYear, this.viewMonth, 1)
      const start = new Date(first)
      start.setDate(1 - first.getDay())
      const days = []
      for (let i = 0; i < 42; i++) {
        const d = new Date(start)
        d.setDate(start.getDate() + i)
        const isSunday = d.getDay() === 0
        const iso = d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
        const inMonth = d.getMonth() === this.viewMonth
        const isToday = iso === this.todayIso
        const isClosest = iso === this.closestPastSunday
        const hasEntry = this.archiveSet.has(iso)
        const pastOrFutureSunday = isSunday && hasEntry
        const classes = {
          'out': !inMonth,
          'is-today': isToday,
          'is-closest': isClosest && isSunday,
          'has-entry': pastOrFutureSunday && !isClosest,
          'no-entry': isSunday && !hasEntry && !isClosest,
          'not-sunday': !isSunday
        }
        days.push({
          key: iso,
          date: d,
          isSunday,
          isClickable: isSunday,
          iso,
          classes
        })
      }
      return days
    }
  },
  methods: {
    prevMonth() {
      const m = new Date(this.viewYear, this.viewMonth - 1, 1)
      this.viewYear = m.getFullYear()
      this.viewMonth = m.getMonth()
    },
    nextMonth() {
      const m = new Date(this.viewYear, this.viewMonth + 1, 1)
      this.viewYear = m.getFullYear()
      this.viewMonth = m.getMonth()
    },
    onSelect(day) {
      if (!day.isSunday) return
      this.$emit('select', day.iso)
    }
  }
}
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  backdrop-filter: blur(3px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal {
  background: #fff;
  border-radius: 10px;
  width: 90%;
  max-width: 600px;
  padding: 1rem;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
}
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}
.modal-header button {
  background: #2c3e50;
  color: white;
  border: 0;
  border-radius: 6px;
  width: 32px;
  height: 32px;
  cursor: pointer;
}
.weekdays, .grid { display: grid; grid-template-columns: repeat(7, 1fr); gap: 6px; }
.weekdays span { text-align: center; font-weight: 600; color: #555; }
.cell { padding: 0.6rem 0; border-radius: 6px; border: 0; cursor: pointer; }
.cell.out { opacity: 0.35; }
.cell.not-sunday { background: #eee; cursor: default; }
.cell.is-today { outline: 2px solid #e74c3c; }
.cell.is-closest { background: #2ecc71; color: white; }
.cell.has-entry { background: #f1c40f; }
.cell.no-entry { background: #555; color: white; }
.legend { display: flex; gap: 1rem; margin-top: 0.75rem; font-size: 0.85rem; align-items: center; flex-wrap: wrap; }
.legend-item { padding: 0.25rem 0.5rem; border-radius: 4px; }
.legend-item.today { outline: 2px solid #e74c3c; }
.legend-item.closest { background: #2ecc71; color: white; }
.legend-item.has-entry { background: #f1c40f; }
.legend-item.none { background: #555; color: white; }
</style>


