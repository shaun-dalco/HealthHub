<?php

declare(strict_types=1);

namespace OCA\HealthHub\AppInfo;

use OCP\AppFramework\App;
use OCP\AppFramework\Bootstrap\IBootContext;
use OCP\AppFramework\Bootstrap\IBootstrap;
use OCP\AppFramework\Bootstrap\IRegistrationContext;

use OCA\HealthHub\Db\HeartRateMapper;
use OCA\HealthHub\Db\OverviewMapper;
use OCA\HealthHub\Db\StepsMapper;
use OCA\HealthHub\Db\SleepMapper;
use OCA\HealthHub\Db\WeightMapper;
use OCA\HealthHub\Service\HealthDataService;

/**
 * Class Application
 *
 * @package OCA\HealthHub\AppInfo
 */
class Application extends App implements IBootstrap {

	public const APP_ID = 'healthhub';

	public function __construct(array $urlParams = []) {
		parent::__construct(self::APP_ID, $urlParams);
	}

	public function register(IRegistrationContext $context): void {
		$context->registerService(HeartRateMapper::class, function($c) {
			return new HeartRateMapper($c->getServer()->getDatabaseConnection());
		});
		$context->registerService(OverviewMapper::class, function($c) {
			return new OverviewMapper($c->getServer()->getDatabaseConnection());
		});
		$context->registerService(StepsMapper::class, function($c) {
			return new StepsMapper($c->getServer()->getDatabaseConnection());
		});
		$context->registerService(SleepMapper::class, function($c) {
			return new SleepMapper($c->getServer()->getDatabaseConnection());
		});
		$context->registerService(WeightMapper::class, function($c) {
			return new WeightMapper($c->getServer()->getDatabaseConnection());
		});

		$context->registerService(HealthDataService::class, function($c) {
			return new HealthDataService(
				$c->query(HeartRateMapper::class),
				$c->query(OverviewMapper::class),
				$c->query(StepsMapper::class),
				$c->query(SleepMapper::class),
				$c->query(WeightMapper::class),
			);
		});
	}

	public function boot(IBootContext $context): void {
	}
}