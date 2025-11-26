<?php

declare(strict_types=1);

namespace OCA\HealthHub\Db;

use OCP\AppFramework\Db\QBMapper;
use OCP\DB\Exception;
use OCP\DB\QueryBuilder\IQueryBuilder;
use OCP\IDBConnection;

class OverviewMapper extends QBMapper {

	public function __construct(IDBConnection $db) {
		parent::__construct($db, 'healthhub_overview', Overview::class);
	}

	/**
	 * @param string $userId
	 * @return Overview[]
	 * @throws Exception
	 */
	public function getOverviewsForUser(string $userId): array {
		$qb = $this->db->getQueryBuilder();
		$qb->select('*')
			->from($this->getTableName())
			->where(
				$qb->expr()->eq('user_id', $qb->createNamedParameter($userId, IQueryBuilder::PARAM_STR))
			)
			->orderBy('date', 'DESC');

		return $this->findEntities($qb);
	}

	/**
	 * @param string $userId
	 * @return Overview|null
	 * @throws Exception
	 */
	public function getLatestOverviewForUser(string $userId): ?Overview {
		$qb = $this->db->getQueryBuilder();
		$qb->select('*')
			->from($this->getTableName())
			->where(
				$qb->expr()->eq('user_id', $qb->createNamedParameter($userId, IQueryBuilder::PARAM_STR))
			)
			->orderBy('date', 'DESC')
			->setMaxResults(1);

		/** @var Overview[] $results */
		$results = $this->findEntities($qb);
		return $results[0] ?? null;
	}

	public function createOverview(
		string $userId,
		string $date,
		int $totalSteps,
		int $avgHr,
		int $maxHr,
		int $minHr,
		float $sleepHours,
		float $weightKg,
		int $updatedAt
	): Overview {
		$overview = new Overview();
		$overview->setUserId($userId);
		$overview->setDate($date);
		$overview->setTotalSteps($totalSteps);
		$overview->setAvgHr($avgHr);
		$overview->setMaxHr($maxHr);
		$overview->setMinHr($minHr);
		$overview->setSleepHours($sleepHours);
		$overview->setWeightKg($weightKg);
		$overview->setUpdatedAt($updatedAt);

		return $this->insert($overview);
	}
}
