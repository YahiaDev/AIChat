<template>
  <q-layout view="lHh Lpr lFf">
    <q-header elevated>
      <q-toolbar>
        <q-btn flat dense round icon="menu" aria-label="Menu" @click="toggleLeftDrawer" />

        <q-toolbar-title>
          AI Chat
        </q-toolbar-title>

        <!--div>Quasar v{{ $q.version }}</div-->
        <div>©Yahia Ammar's world</div>
      </q-toolbar>
    </q-header>

    <q-drawer v-model="leftDrawerOpen" show-if-above bordered>
      <q-list>
        <q-item-label header>
          <!--Essential Links-->
        </q-item-label>
        <q-item to="/" exact>
          <q-item-section avatar>
            <q-icon name="chat" />
          </q-item-section>

          <q-item-section>
            AI Chat
          </q-item-section>
        </q-item>

      </q-list>
    </q-drawer>

    <q-page-container>
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
//import EssentialLink from 'components/EssentialLink.vue';

/*const linksList = [
   {
     title: 'About As',
     caption: 'apps',
     icon: 'school',
     link: '/aboutAs'
   }


];*/

export default defineComponent({
  name: 'MainLayout',

  components: {
    //EssentialLink
  },

  setup() {
    const leftDrawerOpen = ref(false)

    return {
      //essentialLinks: linksList,
      leftDrawerOpen,
      toggleLeftDrawer() {
        leftDrawerOpen.value = !leftDrawerOpen.value
      }
    }
  }
});
</script>
