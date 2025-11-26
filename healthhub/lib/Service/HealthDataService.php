<?php

declare(strict_types=1);

namespace OCA\HealthHub\Service;

use DateTimeImmutable;
use OCA\HealthHub\Db\HeartRateMapper;
// (add other mappers as you create them)
use OCP\DB\Exception;

class HealthDataService {

	public function __construct(
		private HeartRateMapper $heartRateMapper,
		// private OverviewMapper $overviewMapper,
		// private StepsMapper $stepsMapper,
		// private SleepMapper $sleepMapper,
		// private WeightMapper $weightMapper,
	) {
	}

	/**
	 * Main function you’ll call from the controller.
	 */
	public function getHealthDataForUser(string $userId): array {
		// 1. Seed DB on first run, if wanted
		$this->seedIfEmpty($userId);

		// 2. Fetch each section and convert to the same structure
		$overview = $this->getOverviewFromDb($userId);
		$heartRate = $this->getHeartRateFromDb($userId);
		$steps = $this->getStepsFromDb($userId);
		$sleep = $this->getSleepFromDb($userId);
		$weight = $this->getWeightFromDb($userId);

		return [
			'overview' => $overview,
			'heartRate' => $heartRate,
			'steps' => $steps,
			'sleep' => $sleep,
			'weight' => $weight,
		];
	}

	private function seedIfEmpty(string $userId): void {
		// Example: only seed heart rate if empty for this user
		// In reality, you’d also seed steps/sleep/weight/overview similarly.
		try {
			$samples = $this->heartRateMapper->getSamplesForUser($userId);
		} catch (Exception $e) {
			return;
		}

		if (count($samples) > 0) {
			return;
		}

		// === SEED DATA (copied from your dummy array) ===
		// Heart rate
		$this->heartRateMapper->createSample($userId, strtotime('2025-11-23T06:00:00Z'), 60);
		$this->heartRateMapper->createSample($userId, strtotime('2025-11-23T07:00:00Z'), 65);
		$this->heartRateMapper->createSample($userId, strtotime('2025-11-23T08:00:00Z'), 72);
		$this->heartRateMapper->createSample($userId, strtotime('2025-11-23T09:00:00Z'), 78);
		$this->heartRateMapper->createSample($userId, strtotime('2025-11-23T10:00:00Z'), 70);

		// TODO: in the same function, once you have the other mappers:
		// - seed overview row
		// - seed steps rows
		// - seed sleep rows
		// - seed weight rows
	}

	private function getHeartRateFromDb(string $userId): array {
		$samples = $this->heartRateMapper->getSamplesForUser($userId);

		$out = [];
		foreach ($samples as $s) {
			$out[] = [
				'timestamp' => gmdate('Y-m-d\TH:i:s\Z', $s->getTimestamp()),
				'bpm' => $s->getBpm(),
			];
		}
		return $out;
	}

	private function getOverviewFromDb(string $userId): array {
		// For now you can either:
		// 1) store a single overview row in DB and read it here, OR
		// 2) compute overview from steps + heart rate + sleep + weight.
		//
		// To keep this short, assume you have an OverviewMapper with
		// getLatestOverviewForUser($userId)

		// PSEUDO (fill in once you have OverviewMapper):
		/*
		$overview = $this->overviewMapper->getLatestOverviewForUser($userId);
		return [
			'totalStepsToday'     => $overview->getTotalSteps(),
			'avgHeartRateToday'   => $overview->getAvgHr(),
			'maxHeartRateToday'   => $overview->getMaxHr(),
			'minHeartRateToday'   => $overview->getMinHr(),
			'sleepLastNightHours' => $overview->getSleepHours(),
			'weightKg'            => $overview->getWeightKg(),
			'updatedAt'           => gmdate('Y-m-d\TH:i:s\Z', $overview->getUpdatedAt()),
		];
		*/

		// Temporary stub until you implement OverviewMapper
		return [
			'totalStepsToday'     => 0,
			'avgHeartRateToday'   => 0,
			'maxHeartRateToday'   => 0,
			'minHeartRateToday'   => 0,
			'sleepLastNightHours' => 0.0,
			'weightKg'            => 0.0,
			'updatedAt'           => gmdate('Y-m-d\TH:i:s\Z', time()),
		];
	}

	private function getStepsFromDb(string $userId): array {
		// Similar to heart rate: use StepsMapper->getForUser($userId)
		return [];
	}

	private function getSleepFromDb(string $userId): array {
		// Similar
		return [];
	}

	private function getWeightFromDb(string $userId): array {
		// Similar
		return [];
	}
}
