<script lang="ts">
	import { onMount } from 'svelte'; import { auth } from '$lib/stores/auth.svelte'; import Alert from '$lib/components/ui/Alert.svelte'; let { children } = $props(); let allowed = $state(false); let checked = $state(false);
	onMount(async () => { if (!auth.ready) await auth.restore(); allowed = auth.isAdmin; checked = true; });
</script>
{#if !checked}<p class="text-sm text-slate-500">Checking access…</p>{:else if !allowed}<Alert>You need an administrator account to view this page.</Alert>{:else}<div class="space-y-6"><nav class="flex flex-wrap gap-3 text-sm"><a class="text-cyan-800 underline" href="/admin">Overview</a><a class="text-cyan-800 underline" href="/admin/services">Services</a><a class="text-cyan-800 underline" href="/admin/employees">Employees</a><a class="text-cyan-800 underline" href="/admin/bookings">Bookings</a><a class="text-cyan-800 underline" href="/admin/users">Users</a></nav>{@render children()}</div>{/if}
