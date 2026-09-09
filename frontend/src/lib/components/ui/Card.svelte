<script lang="ts">import type { Snippet } from 'svelte'; let { children }: { children?: Snippet } = $props();</script>
<div class="rounded-lg border bg-white p-5 shadow-sm">{@render children?.()}</div>
