<?php

declare(strict_types=1);

namespace OCA\HealthHub\Controller;

use OCA\HealthHub\AppInfo\Application;
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

	public function __construct(
		string $appName,
		IRequest $request,
		IInitialState $initialStateService,
		IConfig $config,
		IAppConfig $appConfig,
		?string $userId,
	) {
		parent::__construct($appName, $request);

		$this->initialStateService = $initialStateService;
		$this->config = $config;
		$this->appConfig = $appConfig;
		$this->userId = $userId;
	}

	/**
	 * Main app page: provide dummy health data + template.
	 *
	 * Frontend loads this with:
	 *   loadState('healthhub', 'healthhub_initial_state')
	 */
	#[NoCSRFRequired]
	#[NoAdminRequired]
	#[FrontpageRoute(verb: 'GET', url: '/')]
	public function index(): TemplateResponse {
		// 1) Build health data (dummy for now)
		$dummyHealthData = $this->getDummyHealthData();

		// 2) Expose it to the frontend via initialState
		//    JS: loadState('healthhub', 'healthhub_initial_state')
		$this->initialStateService->provideInitialState(
			'healthhub_initial_state',
			$dummyHealthData,
		);

		// 3) App version for display / debugging
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

	/**
	 * Dummy health data for testing UI wiring.
	 *
	 * This structure matches what MyMainContent.vue expects:
	 *   - overview
	 *   - heartRate[]
	 *   - steps[]
	 *   - sleep[]
	 *   - weight[]
	 */
	private function getDummyHealthData(): array {
		return [
			// High-level summary (for "Overview")
			'overview' => [
				'totalStepsToday'     => 10432,
				'avgHeartRateToday'   => 72,
				'maxHeartRateToday'   => 138,
				'minHeartRateToday'   => 58,
				'sleepLastNightHours' => 7.4,
				'weightKg'            => 78.3,
				'updatedAt'           => '2025-11-23T09:00:00Z',
			],

			// Heart rate timeseries (for "Heart rate" menu item)
			'heartRate' => [
				['timestamp' => '2025-11-23T06:00:00Z', 'bpm' => 60],
				['timestamp' => '2025-11-23T07:00:00Z', 'bpm' => 65],
				['timestamp' => '2025-11-23T08:00:00Z', 'bpm' => 72],
				['timestamp' => '2025-11-23T09:00:00Z', 'bpm' => 78],
				['timestamp' => '2025-11-23T10:00:00Z', 'bpm' => 70],
			],

			// Daily step counts (for "Steps")
			'steps' => [
				['date' => '2025-11-19', 'count' => 8350],
				['date' => '2025-11-20', 'count' => 10231],
				['date' => '2025-11-21', 'count' => 9432],
				['date' => '2025-11-22', 'count' => 12004],
				['date' => '2025-11-23', 'count' => 10432],
			],

			// Sleep sessions (for "Sleep")
			'sleep' => [
				['date' => '2025-11-19', 'durationHours' => 6.8, 'qualityScore' => 78],
				['date' => '2025-11-20', 'durationHours' => 7.1, 'qualityScore' => 82],
				['date' => '2025-11-21', 'durationHours' => 5.9, 'qualityScore' => 70],
				['date' => '2025-11-22', 'durationHours' => 7.4, 'qualityScore' => 85],
			],

			// Weight trend (for "Weight")
			'weight' => [
				['date' => '2025-11-15', 'kg' => 79.2],
				['date' => '2025-11-17', 'kg' => 78.9],
				['date' => '2025-11-19', 'kg' => 78.5],
				['date' => '2025-11-21', 'kg' => 78.4],
				['date' => '2025-11-23', 'kg' => 78.3],
			],
		];
	}
}
