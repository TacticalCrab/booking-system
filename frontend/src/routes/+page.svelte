<script lang="ts">
  import { onMount } from 'svelte';
  import { createBooking } from '$lib/api/bookings';
  import { availability, employees } from '$lib/api/employees';
  import { services } from '$lib/api/services';
  import type { Employee, Service } from '$lib/api/types';
  import { auth } from '$lib/stores/auth.svelte';
  import Button from '$lib/components/ui/Button.svelte';
  import Card from '$lib/components/ui/Card.svelte';
  import Input from '$lib/components/ui/Input.svelte';
  import Select from '$lib/components/ui/Select.svelte';
  import Alert from '$lib/components/ui/Alert.svelte';

  let staff = $state<Employee[]>([]);
  let offerings = $state<Service[]>([]);
  let employeeId = $state('');
  let serviceId = $state('');
  let date = $state('');
  let time = $state('');
  let slots = $state<string[]>([]);
  let loading = $state(true);
  let loadingAvailability = $state(false);
  let saving = $state(false);
  let error = $state('');
  let availabilityError = $state('');
  let success = $state('');
  let availabilityRequest = 0;

  let availableServices = $derived(employeeId ? staff.find((employee) => employee.id === Number(employeeId))?.services ?? [] : offerings);

  onMount(async () => {
    try {
      [staff, offerings] = await Promise.all([employees().then((page) => page.content), services().then((page) => page.content)]);
    } catch (reason) {
      error = reason instanceof Error ? reason.message : 'Could not load booking options.';
    } finally {
      loading = false;
    }
  });

  function clearAvailability() {
    slots = [];
    time = '';
    availabilityError = '';
  }

  function changeEmployee() {
    if (!availableServices.some((service) => service.id === Number(serviceId))) serviceId = '';
    clearAvailability();
  }

  async function loadAvailability() {
    const requestId = ++availabilityRequest;
    clearAvailability();
    if (!employeeId || !serviceId || !date) return;

    loadingAvailability = true;
    try {
      const response = await availability(Number(employeeId), Number(serviceId), date);
      if (requestId === availabilityRequest) slots = response.slots.map((slot) => slot.slice(0, 5));
    } catch (reason) {
      if (requestId === availabilityRequest) availabilityError = reason instanceof Error ? reason.message : 'Could not load available times.';
    } finally {
      if (requestId === availabilityRequest) loadingAvailability = false;
    }
  }

  $effect(() => {
    employeeId;
    serviceId;
    date;
    void loadAvailability();
  });

  async function submit() {
    error = '';
    success = '';
    if (!auth.user) {
      error = 'Please log in before creating a booking.';
      return;
    }
    if (!employeeId || !serviceId || !date || !time) {
      error = 'Choose an employee, service, date, and available time.';
      return;
    }
    if (!slots.includes(time)) {
      error = 'Choose a currently available time.';
      return;
    }

    saving = true;
    try {
      await createBooking(Number(employeeId), Number(serviceId), `${date}T${time}`);
      success = 'Booking created. You can view it on My bookings.';
      time = '';
      await loadAvailability();
    } catch (reason) {
      error = reason instanceof Error ? reason.message : 'Could not create booking.';
    } finally {
      saving = false;
    }
  }
</script>

<div class="mx-auto max-w-2xl space-y-6">
  <div>
    <h1 class="text-2xl font-semibold">Book an appointment</h1>
    <p class="mt-1 text-sm text-slate-600">Choose a service, an employee, and an available future time.</p>
  </div>
  <Card>
    {#if loading}
      <p class="text-sm text-slate-500">Loading booking options…</p>
    {:else}
      <form class="space-y-4" onsubmit={(event) => { event.preventDefault(); submit(); }}>
        {#if error}<Alert>{error}</Alert>{/if}
        {#if success}<Alert tone="success">{success}</Alert>{/if}
        <label class="block text-sm font-medium">
          Employee
          <Select bind:value={employeeId} onchange={changeEmployee}>
            <option value="">Select an employee</option>
            {#each staff as employee}<option value={employee.id}>{employee.name}</option>{/each}
          </Select>
        </label>
        <label class="block text-sm font-medium">
          Service
          <Select bind:value={serviceId} onchange={clearAvailability}>
            <option value="">Select a service</option>
            {#each availableServices as service}<option value={service.id}>{service.name} ({service.durationMinutes} min)</option>{/each}
          </Select>
        </label>
        <label class="block text-sm font-medium">
          Date
          <Input bind:value={date} type="date" min={new Date().toISOString().slice(0, 10)} />
        </label>
        {#if employeeId && serviceId && date}
          <div class="space-y-2">
            <p class="text-sm font-medium">Available times</p>
            {#if loadingAvailability}
              <p class="text-sm text-slate-500">Loading available times…</p>
            {:else if availabilityError}
              <Alert>{availabilityError}</Alert>
            {:else if slots.length === 0}
              <p class="text-sm text-slate-500">No times are available on this date.</p>
            {:else}
              <div class="flex flex-wrap gap-2">
                {#each slots as slot}
                  <Button type="button" variant={time === slot ? 'default' : 'outline'} onclick={() => time = slot}>{slot}</Button>
                {/each}
              </div>
            {/if}
          </div>
        {/if}
        <Button type="submit" disabled={saving || !auth.ready || !time}>{saving ? 'Creating…' : 'Create booking'}</Button>
      </form>
    {/if}
  </Card>
</div>
