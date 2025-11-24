<?php
use OCP\Util;

$appId = OCA\HealthHub\AppInfo\Application::APP_ID;
Util::addScript($appId, $appId . '-main');
Util::addStyle($appId, 'main');
?>

<div id="app" class="healthhub">

    <!-- Sidebar / navigation panel -->
    <div id="app-navigation">
        <ul class="app-navigation-list" id="healthhub-sidebar">
            <li class="app-navigation-entry" data-section="overview">
                <a href="#" class="app-navigation-entry-link">
                    <span class="app-navigation-entry__title">Overview</span>
                </a>
            </li>
            <li class="app-navigation-entry" data-section="heart-rate">
                <a href="#" class="app-navigation-entry-link">
                    <span class="app-navigation-entry__title">Heart rate</span>
                </a>
            </li>
            <li class="app-navigation-entry" data-section="steps">
                <a href="#" class="app-navigation-entry-link">
                    <span class="app-navigation-entry__title">Steps</span>
                </a>
            </li>
            <li class="app-navigation-entry" data-section="sleep">
                <a href="#" class="app-navigation-entry-link">
                    <span class="app-navigation-entry__title">Sleep</span>
                </a>
            </li>
            <li class="app-navigation-entry" data-section="weight">
                <a href="#" class="app-navigation-entry-link">
                    <span class="app-navigation-entry__title">Weight</span>
                </a>
            </li>
            <!-- Add more categories as needed -->
        </ul>
    </div>

    <!-- Main content panel -->
    <div id="app-content">
        <div id="healthhub-main-panel">
            <div id="healthhub"></div>
        </div>
    </div>


</div>
