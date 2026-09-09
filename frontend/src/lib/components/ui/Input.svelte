<script lang="ts"> import type { HTMLInputAttributes } from 'svelte/elements'; let { class: className = '', value = $bindable(), ...rest }: HTMLInputAttributes = $props(); </script>
<input bind:value class={`flex h-9 w-full rounded-md border bg-white px-3 py-1 text-sm outline-none focus:ring-2 focus:ring-cyan-700 ${className}`} {...rest} />
