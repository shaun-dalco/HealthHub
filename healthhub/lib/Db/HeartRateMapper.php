<?php

declare(strict_types=1);

namespace OCA\HealthHub\Db;

use DateTimeImmutable;
use OCP\AppFramework\Db\QBMapper;
use OCP\DB\Exception;
use OCP\DB\QueryBuilder\IQueryBuilder;
use OCP\IDBConnection;

class HeartRateMapper extends QBMapper {

	public function __construct(IDBConnection $db) {
		parent::__construct($db, 'healthhub_heart_rate', HeartRateSample::class);
	}

	/**
	 * @param string $userId
	 * @return HeartRateSample[]
	 * @throws Exception
	 */
	public function getSamplesForUser(string $userId): array {
		$qb = $this->db->getQueryBuilder();
		$qb->select('*')
			->from($this->getTableName())
			->where(
				$qb->expr()->eq('user_id', $qb->createNamedParameter($userId, IQueryBuilder::PARAM_STR))
			)
			->orderBy('timestamp', 'ASC');

		return $this->findEntities($qb);
	}

	/**
	 * Seed a single sample
	 */
	public function createSample(string $userId, int $timestamp, int $bpm): HeartRateSample {
		$sample = new HeartRateSample();
		$sample->setUserId($userId);
		$sample->setTimestamp($timestamp);
		$sample->setBpm($bpm);
		return $this->insert($sample);
	}
}
