<template>
  <span class="loading-dots" aria-label="加载中">
    <i></i><i></i><i></i>
  </span>
</template>

<script>
export default {
  name: 'LoadingDots'
}
</script>

<style scoped>
.loading-dots {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 2px;
}

.loading-dots i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--green-500);
  animation: bounce 1.2s infinite ease-in-out;
}

.loading-dots i:nth-child(2) {
  animation-delay: 0.15s;
  background: var(--emerald-400);
}

.loading-dots i:nth-child(3) {
  animation-delay: 0.3s;
  background: var(--mint-200);
}

@keyframes bounce {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.6;
  }
  30% {
    transform: translateY(-5px);
    opacity: 1;
  }
}
</style>