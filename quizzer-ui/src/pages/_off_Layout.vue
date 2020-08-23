<template>
  <div class="wrapper" >
    <div class="main-panel">
      <top-navbar></top-navbar>

      <dashboard-content></dashboard-content>

      <content-footer></content-footer>
    </div>
  </div>
</template>
<style lang="scss"></style>
<script>
import ContentFooter from "./ContentFooter.vue";
import DashboardContent from "./Content.vue";
import MobileMenu from "@/pages/Layout/MobileMenu.vue";
import TopNavbar from "@/pages/Layout/TopNavbar.vue";

export default {
  components: {
    DashboardContent,
    ContentFooter,
    MobileMenu,
    TopNavbar
  }
};
</script>
