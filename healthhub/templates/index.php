<?php

use OCP\Util;

/** @var array $_ */
$appId = \OCA\HealthHub\AppInfo\Application::APP_ID;

// Load bundled JS + CSS
Util::addScript($appId, $appId . '-main'); // e.g. healthhub-main.js
Util::addStyle($appId, 'main');            // your CSS
?>

<div id="app" class="healthhub">
    <div id="app-content">
        <!-- Vue mounts into this element -->
        <div id="healthhub"></div>
    </div>
</div>
