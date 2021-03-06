package lt.swedbank.itacademy.service;

import lt.swedbank.itacademy.domain.*;
import lt.swedbank.itacademy.util.LoanComparator;
import lt.swedbank.itacademy.util.LoanUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class LoanService implements LoanServiceInterface {

    private Loan[] loans;

    public LoanService(Loan[] loans) {
        this.loans = loans;
    }

    @Override
    public List<Loan> getHighRiskLoans() {
        return getLoansByRiskType(LoanRiskType.HIGH_RISK);
    }

    @Override
    public BigDecimal calculateAverageLoanCost() {
        //When working with BigDecimal, always use provided constants for 0, 1, 10 (ex. BigDecimal.ZERO). Short answer: better performance.
        BigDecimal loanSum = new BigDecimal("0");
        for (Loan loan : loans) {
            loanSum = loanSum.add(loan.calculateTotalLoanCost());
        }
        return loanSum.divide(BigDecimal.valueOf(loans.length));
    }

    @Override
    public BigDecimal calculateAverageLoanCost(LoanRiskType riskType) {
        //Use BigDecimal.ZERO here as well
        BigDecimal loanSum = new BigDecimal("0");
        for (Loan loan : getLoansByRiskType(riskType)) {
            loanSum = loanSum.add(loan.calculateTotalLoanCost());
        }

        return loanSum.divide(BigDecimal.valueOf(getLoansByRiskType(riskType).size()));
    }

    @Override
    public BigDecimal getMaximumPriceOfNonExpiredLoans() {
        BigDecimal maxLoan = BigDecimal.ZERO;
        for (Loan loan : loans) {
            if (loan.isValid() && loan.getPrice().compareTo(maxLoan) > 0) {
                maxLoan = loan.getPrice();
            }
        }
        return maxLoan;
    }

    @Override
    public Map<LoanRiskType, List<Loan>> groupLoansByRiskType() {
        Map<LoanRiskType, List<Loan>> loansGroupedByRiskType = new HashMap<>();
        //Hm.. almost correct :)
        //Problem with this, is that your "loansGroupedByRiskType" will always have a fixed size = "LoanRiskType.values().length".
        //Now imagine that you have 100 "LoanRiskType" values and 1 Loan of some "LoanRiskType". What then?
        //This approach works with test data, but not in the real life :)
        for (LoanRiskType riskType : LoanRiskType.values()) {
            loansGroupedByRiskType.put(riskType, getLoansByRiskType(riskType));
        }
        return loansGroupedByRiskType;
    }

    @Override
    public Set<String> findVehicleModels() {
        Set<String> vehicleModels = new HashSet<>();
        for (Loan loan : loans) {
            if (loan instanceof VehicleLoan) {
                vehicleModels.add(((VehicleLoan) loan).getModel());
            }
        }
        return vehicleModels;
    }

    @Override
    public List<VehicleLoan> getNormalRiskVehicleLoans() {
        return getVehicleLoansByRiskType(LoanRiskType.NORMAL_RISK);
    }

    @Override
    public int getMaximumAgeOfLowRiskLoanedVehicles() {
        List<VehicleLoan> lowRiskVehicleLoans = getVehicleLoansByRiskType(LoanRiskType.LOW_RISK);
        int maxAge = 0;
        for (VehicleLoan vehicleLoan : lowRiskVehicleLoans) {
            if (vehicleLoan.getAge() > maxAge) {
                maxAge = vehicleLoan.getAge();
            }
        }
        return maxAge;
    }

    @Override
    public List<VehicleLoan> getExpiredHighRiskVehicleLoansOfHighestDuration() {
        List<VehicleLoan> expiredHighRiskVehicleLoans = new ArrayList<>();
        int termInYears = 0;
        for (VehicleLoan vehicleLoan : getVehicleLoansByRiskType(LoanRiskType.HIGH_RISK)) {
            if (!vehicleLoan.isValid()) {
                if (vehicleLoan.getTermInYears() > termInYears) {
                    expiredHighRiskVehicleLoans.clear();
                    termInYears = vehicleLoan.getTermInYears();
                    expiredHighRiskVehicleLoans.add(vehicleLoan);
                } else if (vehicleLoan.getTermInYears() == termInYears) {
                    expiredHighRiskVehicleLoans.add(vehicleLoan);
                }
            }

        }
        return expiredHighRiskVehicleLoans;
    }

    @Override
    public List<Loan> getPersonalRealEstateLoans() {
        List<Loan> personalRealEstateLoans = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan instanceof RealEstateLoan && ((RealEstateLoan) loan).getPurpose() == RealEstatePurpose.PERSONAL) {
                personalRealEstateLoans.add(loan);
            }
        }
        return personalRealEstateLoans;
    }

    @Override
    public List<HarvesterLoan> getLowRiskHarvesterLoans() {
        List<HarvesterLoan> lowRiskHarvesterLoans = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan instanceof HarvesterLoan && loan.getRiskType() == LoanRiskType.LOW_RISK) {
                lowRiskHarvesterLoans.add((HarvesterLoan) loan);
            }
        }
        return lowRiskHarvesterLoans;
    }

    @Override
    public List<Loan> getExpiredLandLoansInReservation() {
        List<Loan> expiredLandLoansInReservation = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan instanceof LandLoan && !loan.isValid() && ((LandLoan) loan).isInReservation()) {
                expiredLandLoansInReservation.add(loan);
            }
        }
        return expiredLandLoansInReservation;
    }

    @Override
    public List<VehicleLoan> getLoansOfHigherThanAverageDepreciation() {
        List<VehicleLoan> loansOfHigherThanAverageDepreciation = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan instanceof VehicleLoan && LoanUtil.calculateVehicleDepreciation((VehicleLoan) loan).compareTo(calculateAverageDepreciation()) > 0) {
                loansOfHigherThanAverageDepreciation.add((VehicleLoan) loan);
            }
        }
        return loansOfHigherThanAverageDepreciation;
    }

    private List<Loan> getLoansByRiskType(LoanRiskType riskType) {
        List<Loan> loansByRiskType = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan.getRiskType().equals(riskType)) {
                loansByRiskType.add(loan);
            }
        }
        return loansByRiskType;
    }

    private List<VehicleLoan> getVehicleLoansByRiskType(LoanRiskType riskType) {
        List<VehicleLoan> vehicleLoansByRiskType = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan instanceof VehicleLoan && loan.getRiskType().equals(riskType)) {
                vehicleLoansByRiskType.add((VehicleLoan) loan);
            }
        }
        return vehicleLoansByRiskType;

    }

    //Missing from LoanServiceInterface
    public BigDecimal calculateAverageDepreciation() {
        //Use BigDecimal.ZERO here as well
        BigDecimal averageDepreciation = new BigDecimal("0");
        int count = 0;
        for (Loan loan : loans) {
            if (loan instanceof VehicleLoan) {
                averageDepreciation = averageDepreciation.add(LoanUtil.calculateVehicleDepreciation((VehicleLoan) loan));
                count++;
            }
        }
        averageDepreciation = averageDepreciation.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        return averageDepreciation;
    }

    //Missing from LoanServiceInterface
    public Set<Loan> prioritizeLoans() {
        Set<Loan> sortedLoans = new TreeSet<>(new LoanComparator());
        sortedLoans.addAll(Arrays.asList(loans));
        return sortedLoans;
    }
}
