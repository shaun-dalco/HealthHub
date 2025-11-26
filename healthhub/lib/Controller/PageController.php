<?php

declare(strict_types=1);

namespace OCA\HealthHub\Controller;

use OCA\HealthHub\AppInfo\Application;
use OCA\HealthHub\Service\HealthDataService;
use OCP\AppFramework\Controller;
use OCP\AppFramework\Http\Attribute\FrontpageRoute;
use OCP\AppFramework\Http\Attribute\NoAdminRequired;
use OCP\AppFramework\Http\Attribute\NoCSRFRequired;
use OCP\AppFramework\Http\TemplateResponse;
use OCP\AppFramework\Services\IInitialState;
use OCP\IAppConfig;
use OCP\IConfig;
use OCP\IRequest;

class PageController extends Controller {

	private IInitialState $initialStateService;
	private IConfig $config;
	private IAppConfig $appConfig;
	private ?string $userId;
	private HealthDataService $healthDataService;

	public function __construct(
		string $appName,
		IRequest $request,
		IInitialState $initialStateService,
		IConfig $config,
		IAppConfig $appConfig,
		HealthDataService $healthDataService,
		?string $userId,
	) {
		parent::__construct($appName, $request);

		$this->initialStateService = $initialStateService;
		$this->config = $config;
		$this->appConfig = $appConfig;
		$this->healthDataService = $healthDataService;
		$this->userId = $userId;
	}

	/**
	 * Main app page: provide health data (now from DB) + template.
	 *
	 * Frontend loads this with:
	 *   loadState('healthhub', 'healthhub_initial_state')
	 */
	#[NoCSRFRequired]
	#[NoAdminRequired]
	#[FrontpageRoute(verb: 'GET', url: '/')]
	public function index(): TemplateResponse {
		// 1) Build health data from the DB-backed service
		if ($this->userId === null) {
			$healthData = [
				'overview'  => [],
				'heartRate' => [],
				'steps'     => [],
				'sleep'     => [],
				'weight'    => [],
			];
		} else {
			$healthData = $this->healthDataService->getHealthDataForUser($this->userId);
		}

		// 2) Expose it to the frontend via initialState
		//    JS: loadState('healthhub', 'healthhub_initial_state')
		$this->initialStateService->provideInitialState(
			'healthhub_initial_state',
			$healthData,
		);

		// 3) App version for display / debugging (same as before)
		$appVersion = $this->appConfig->getValueString(
			Application::APP_ID,
			'installed_version',
			lazy: true,
		);

		return new TemplateResponse(
			Application::APP_ID,
			'index',
			[
				'app_version' => $appVersion,
			],
		);
	}
}
